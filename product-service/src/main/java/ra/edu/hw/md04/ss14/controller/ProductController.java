package ra.edu.hw.md04.ss14.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ra.edu.hw.md04.ss14.dto.Product;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ConcurrentMap<Long, Product> products = new ConcurrentHashMap<>(Map.of(
            1L, new Product(1L, "Laptop", new BigDecimal("1299.99")),
            2L, new Product(2L, "Keyboard", new BigDecimal("89.99")),
            3L, new Product(3L, "Mouse", new BigDecimal("39.99"))
    ));

    @GetMapping
    public List<Product> getProducts() {
        return products.values().stream()
                .sorted((first, second) -> first.id().compareTo(second.id()))
                .toList();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> deleteProduct(@PathVariable Long id) {
        Product removed = products.remove(id);
        if (removed == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return Map.of("message", "Product deleted", "id", id.toString());
    }
}
