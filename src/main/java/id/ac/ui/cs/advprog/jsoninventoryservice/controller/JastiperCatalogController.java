package id.ac.ui.cs.advprog.jsoninventoryservice.controller;

import id.ac.ui.cs.advprog.jsoninventoryservice.dto.request.ProductSearchCriteria;
import id.ac.ui.cs.advprog.jsoninventoryservice.dto.response.ProductResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.AuthIntegrationService;
import id.ac.ui.cs.advprog.jsoninventoryservice.service.ProductService;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ApiResponse;
import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/jastipers")
@RequiredArgsConstructor
public class JastiperCatalogController {
    private final ProductService searchService;
    private final AuthIntegrationService authIntegrationService;

    @GetMapping("/{username}/products")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getJastiperCatalog(
            @PathVariable("username") String username,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(required = false, name = "min_price") Long minPrice,
            @RequestParam(required = false, name = "max_price") Long maxPrice,
            @RequestParam(required = false, name = "category_id") Integer categoryId,
            @RequestParam(required = false, name = "origin_country") String originCountry,
            Pageable pageable) {

        UUID jastiperId = authIntegrationService.getJastiperIdByUsername(username);

                ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                        .keyword(q)
                        .jastiperId(jastiperId)
                        .minPrice(minPrice)
                        .maxPrice(maxPrice)
                        .categoryId(categoryId)
                        .originCountry(originCountry)
                        .build();

        Page<ProductResponse> productPage = searchService.searchProductsPublic(criteria, pageable);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", productPage.getContent());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", productPage.getNumber() + 1);
        pagination.put("limit", productPage.getSize());
        pagination.put("total", productPage.getTotalElements());
        pagination.put("total_pages", productPage.getTotalPages());
        responseData.put("pagination", pagination);

        return ResponseUtil.success(responseData, "Fetched jastiper catalog successfully.");
    }
}