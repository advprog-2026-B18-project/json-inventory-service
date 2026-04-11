package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReleaseRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.StockReserveRequest;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
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
public class StockManagementServiceImpl implements StockManagementService {

    private final ProductRepository productRepository;
    private final StockReservationRepository reservationRepository;

    @Override
    @Transactional
    public Optional<ProductResponse> reserveStock(UUID productId, StockReserveRequest req) {
        Optional<StockReservation> existing = reservationRepository.findByOrderIdAndProduct_Id(req.getOrderId(), productId);

        if (existing.isPresent() && existing.get().getStatus() != ReservationStatus.RELEASED) {
            return productRepository.findByIdForUpdate(productId).map(ProductResponse::fromEntity);
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
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();
            reservationRepository.save(res);

            return ProductResponse.fromEntity(p);
        });
    }

    @Override
    @Transactional
    public Optional<ProductResponse> releaseStock(UUID id, StockReleaseRequest req) {
        Optional<StockReservation> optRes = reservationRepository.findByOrderIdAndProduct_Id(req.getOrderId(), id);

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

            return Optional.of(ProductResponse.fromEntity(p));
        }
        return Optional.empty();
    }
}
