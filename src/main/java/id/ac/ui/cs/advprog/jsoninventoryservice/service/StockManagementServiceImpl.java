package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.PostOrderRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReleaseRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReserveRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.StockOperationResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.exception.StockOperationException;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.StockReservation;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ReservationStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.StockReservationRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.strategy.ShoppingModeProvider;
import id.ac.ui.cs.advprog.jsoninventoryservice.strategy.ShoppingModeStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings("ResultOfMethodCallIgnored")
public class StockManagementServiceImpl implements StockManagementService {
    private final ProductRepository productRepository;
    private final StockReservationRepository reservationRepository;
    private final ShoppingModeProvider shoppingModeProvider;

    private ProductResponse mapToResponseSafe(Product p) {
        if (p.getImages() != null) p.getImages().size();
        if (p.getTags() != null) p.getTags().size();
        return ProductResponse.fromEntity(p);
    }

    @Override
    @Transactional
    public Optional<StockOperationResponse> reserveStock(UUID productId, StockReserveRequest req) {
        Optional<StockReservation> existing = reservationRepository
                .findByOrderIdAndProduct_ProductId(req.getOrderId(), productId);

        if (existing.isPresent() && existing.get().getStatus() != ReservationStatus.RELEASED) {
            return productRepository.findById(productId).map(p -> StockOperationResponse.builder()
                    .productId(p.getProductId())
                    .reservedQuantity(existing.get().getQuantity())
                    .remainingStock(p.getStock())
                    .reservationId(existing.get().getReservationId())
                    .status("RESERVED")
                    .build());
        }

        return productRepository.findById(productId).flatMap(p -> {
            ShoppingModeStrategy strategy = shoppingModeProvider.getStrategy(p.getMode());
            if (!strategy.isEligibleForReservation(p, req.getQuantity())) {
                return Optional.empty();
            }

            int updatedRows = productRepository.reserveStockAtomic(productId, req.getQuantity());
            if (updatedRows == 0) {
                throw new StockOperationException("Insufficient stock!", 404);
            }

            p.setStock(p.getStock() - req.getQuantity());
            if (p.getStock() == 0) {
                p.setStatus(ProductStatus.OUT_OF_STOCK);
                productRepository.updateStatusAtomic(productId, ProductStatus.OUT_OF_STOCK);
            }

            StockReservation res = StockReservation.builder()
                    .product(p)
                    .orderId(req.getOrderId())
                    .quantity(req.getQuantity())
                    .status(ReservationStatus.PENDING)
                    .build();
            res = reservationRepository.save(res);

            return Optional.of(StockOperationResponse.builder()
                    .productId(p.getProductId())
                    .reservedQuantity(res.getQuantity())
                    .remainingStock(p.getStock())
                    .reservationId(res.getReservationId())
                    .status("RESERVED")
                    .build());
        });
    }

    @Override
    @Transactional
    public Optional<ProductResponse> releaseStock(UUID id, StockReleaseRequest req) {
        Optional<StockReservation> optRes = reservationRepository
                .findByOrderIdAndProduct_ProductId(req.getOrderId(), id);

        if (optRes.isPresent() && optRes.get().getStatus() != ReservationStatus.RELEASED) {
            StockReservation res = optRes.get();
            Product p = productRepository.findById(id).orElseThrow();

            productRepository.releaseStockAtomic(id, res.getQuantity());

            p.setStock(p.getStock() + res.getQuantity());
            if (p.getStatus() == ProductStatus.OUT_OF_STOCK && p.getStock() > 0) {
                p.setStatus(ProductStatus.ACTIVE);
                productRepository.updateStatusAtomic(id, ProductStatus.ACTIVE);
            }

            res.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(res);

            return Optional.of(mapToResponseSafe(p));
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<ProductResponse> processPostOrder(UUID productId, PostOrderRequest req) {
        Product p = productRepository.findById(productId).orElse(null);
        if (p == null) return Optional.empty();

        String action = req.getAction();
        if (action == null || (!action.equals("CONFIRM") && !action.equals("CANCEL"))) {
            throw new IllegalArgumentException("Invalid action. Must be 'CONFIRM' or 'CANCEL'.");
        }

        Optional<StockReservation> optRes = reservationRepository
                .findByOrderIdAndProduct_ProductId(req.getOrderId(), productId);

        if (action.equals("CONFIRM")) {
            handleConfirmAction(p, optRes, req);
        } else {
            handleCancelAction(p, optRes);
        }

        return Optional.of(mapToResponseSafe(p));
    }

    private void handleConfirmAction(Product p, Optional<StockReservation> optRes, PostOrderRequest req) {
        if (optRes.isPresent() && optRes.get().getStatus() == ReservationStatus.PENDING) {
            StockReservation res = optRes.get();
            res.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(res);

            int currentOrders = p.getTotalOrders() != null ? p.getTotalOrders() : 0;
            p.setTotalOrders(currentOrders + 1);
        }

        // calculateNewRating handles totalReviews increment internally — self-contained
        if (req.getRating() != null && req.getRating() >= 1.0 && req.getRating() <= 5.0) {
            calculateNewRating(p, req.getRating().floatValue());
        }

        productRepository.save(p);
    }

    // Self-contained: increment totalReviews dan hitung avg di sini sekaligus
    private void calculateNewRating(Product p, float incomingRating) {
        int currentReviews = p.getTotalReviews() != null ? p.getTotalReviews() : 0;
        float currentAvg = p.getAvgRating() != null ? p.getAvgRating() : 0.0f;

        float totalRatingSum = (currentAvg * currentReviews) + incomingRating;
        int newReviews = currentReviews + 1;

        p.setTotalReviews(newReviews);
        p.setAvgRating(totalRatingSum / newReviews);
    }

    private void handleCancelAction(Product p, Optional<StockReservation> optRes) {
        if (optRes.isPresent() && optRes.get().getStatus() == ReservationStatus.PENDING) {
            StockReservation res = optRes.get();
            res.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(res);

            p.setStock(p.getStock() + res.getQuantity());
            if (p.getStatus() == ProductStatus.OUT_OF_STOCK && p.getStock() > 0) {
                p.setStatus(ProductStatus.ACTIVE);
            }
            productRepository.save(p);
        }
    }
}