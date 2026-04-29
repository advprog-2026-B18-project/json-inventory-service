package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReleaseRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReserveRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.StockOperationResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.StockReservation;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ReservationStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.ProductRepository;
import id.ac.ui.cs.advprog.jsoninventoryservice.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.PostOrderRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings("ResultOfMethodCallIgnored")
public class StockManagementServiceImpl implements StockManagementService {
    private final ProductRepository productRepository;
    private final StockReservationRepository reservationRepository;

    private ProductResponse mapToResponseSafe(Product p) {
        if (p.getImages() != null) p.getImages().size();
        if (p.getTags() != null) p.getTags().size();
        return ProductResponse.fromEntity(p);
    }

    @Override
    @Transactional
    public Optional<StockOperationResponse> reserveStock(UUID productId, StockReserveRequest req) {
        Optional<StockReservation> existing = reservationRepository.findByOrderIdAndProduct_ProductId(req.getOrderId(), productId);

        if (existing.isPresent() && existing.get().getStatus() != ReservationStatus.RELEASED) {
            return productRepository.findByIdForUpdate(productId).map(p -> StockOperationResponse.builder()
                    .productId(p.getProductId())
                    .reservedQuantity(existing.get().getQuantity())
                    .remainingStock(p.getStock())
                    .reservationId(existing.get().getReservationId())
                    .status("RESERVED")
                    .build());
        }

        return productRepository.findByIdForUpdate(productId).map(p -> {
            if (p.getStatus() != ProductStatus.ACTIVE || p.getStock() < req.getQuantity()) {
                return null;
            }

            p.setStock(p.getStock() - req.getQuantity());
            if (p.getStock() == 0) p.setStatus(ProductStatus.OUT_OF_STOCK);
            productRepository.save(p);

            StockReservation res = StockReservation.builder()
                    .product(p)
                    .orderId(req.getOrderId())
                    .quantity(req.getQuantity())
                    .status(ReservationStatus.PENDING)
                    .build();
            res = reservationRepository.save(res);

            return StockOperationResponse.builder()
                    .productId(p.getProductId())
                    .reservedQuantity(res.getQuantity())
                    .remainingStock(p.getStock())
                    .reservationId(res.getReservationId())
                    .status("RESERVED")
                    .build();
        });
    }

    @Override
    @Transactional
    public Optional<ProductResponse> releaseStock(UUID id, StockReleaseRequest req) {
        Optional<StockReservation> optRes = reservationRepository.findByOrderIdAndProduct_ProductId(req.getOrderId(), id);

        if (optRes.isPresent() && optRes.get().getStatus() != ReservationStatus.RELEASED) {
            StockReservation res = optRes.get();
            Product p = productRepository.findByIdForUpdate(id).orElseThrow();

            boolean isPhysicalStockEmpty = "OUT_OF_STOCK".equalsIgnoreCase(req.getReason());

            if (!isPhysicalStockEmpty) {
                p.setStock(p.getStock() + res.getQuantity());
                if (p.getStatus() == ProductStatus.OUT_OF_STOCK && p.getStock() > 0) {
                    p.setStatus(ProductStatus.ACTIVE);
                }
            } else {
                p.setStock(0);
                p.setStatus(ProductStatus.OUT_OF_STOCK);
            }

            productRepository.save(p);
            res.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(res);

            return Optional.of(mapToResponseSafe(p));
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<ProductResponse> processPostOrder(UUID id, PostOrderRequest request) {
        Optional<Product> optProduct = productRepository.findByIdForUpdate(id);
        if (optProduct.isEmpty()) return Optional.empty();
        Product product = optProduct.get();
        Optional<StockReservation> optRes = reservationRepository.findByOrderIdAndProduct_ProductId(request.getOrderId(), id);

        if ("CONFIRM".equalsIgnoreCase(request.getAction())) {
            handleConfirmAction(product, request, optRes);
        } else if ("CANCEL".equalsIgnoreCase(request.getAction())) {
            handleCancelAction(product, request, optRes);
        } else {
            throw new IllegalArgumentException("Invalid action. Must be 'CONFIRM' or 'CANCEL'.");
        }
        productRepository.save(product);

        return Optional.of(mapToResponseSafe(product));
    }

    private void handleConfirmAction(Product product, PostOrderRequest request, Optional<StockReservation> optRes) {
        if (optRes.isPresent() && optRes.get().getStatus() == ReservationStatus.PENDING) {
            StockReservation res = optRes.get();
            res.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(res);
            int currentTotalOrders = product.getTotalOrders() != null ? product.getTotalOrders() : 0;
            product.setTotalOrders(currentTotalOrders + 1);
        }

        if (request.getRating() != null && request.getRating() >= 1.0 && request.getRating() <= 5.0) {
            int currentReviews = product.getTotalReviews() != null ? product.getTotalReviews() : 0;
            float currentAvg = product.getAvgRating() != null ? product.getAvgRating() : 0.0f;
            float newAvg = ((currentAvg * currentReviews) + request.getRating().floatValue()) / (currentReviews + 1);
            product.setTotalReviews(currentReviews + 1);
            product.setAvgRating(newAvg);
        }
    }

    private void handleCancelAction(Product product, PostOrderRequest request, Optional<StockReservation> optRes) {
        if (optRes.isPresent() && optRes.get().getStatus() != ReservationStatus.RELEASED) {
            StockReservation res = optRes.get();
            boolean isPhysicalStockEmpty = "OUT_OF_STOCK".equalsIgnoreCase(request.getReason());

            if (!isPhysicalStockEmpty) {
                product.setStock(product.getStock() + res.getQuantity());
                if (product.getStatus() == ProductStatus.OUT_OF_STOCK && product.getStock() > 0) {
                    product.setStatus(ProductStatus.ACTIVE);
                }
            } else {
                product.setStock(0);
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            }
            res.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(res);
        }
    }
}