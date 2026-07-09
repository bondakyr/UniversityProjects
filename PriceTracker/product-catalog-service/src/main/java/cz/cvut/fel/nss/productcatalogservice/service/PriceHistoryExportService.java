package cz.cvut.fel.nss.productcatalogservice.service;

import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.productcatalogservice.entity.PriceRecord;
import cz.cvut.fel.nss.productcatalogservice.entity.Product;
import cz.cvut.fel.nss.productcatalogservice.repository.PriceRecordRepository;
import cz.cvut.fel.nss.productcatalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceHistoryExportService {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ProductRepository productRepository;
    private final PriceRecordRepository priceRecordRepository;

    public String exportAsCsv(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        List<PriceRecord> history = priceRecordRepository.findByProductIdOrderByScrapedAtAsc(productId);

        StringBuilder sb = new StringBuilder();
        sb.append("product_id,product_name,scraped_at,shop_name,price,stock_count,product_url\n");
        for (PriceRecord r : history) {
            sb.append(product.getId()).append(',')
                    .append(escape(product.getName())).append(',')
                    .append(r.getScrapedAt().format(TS_FORMAT)).append(',')
                    .append(escape(r.getShopName())).append(',')
                    .append(r.getPrice()).append(',')
                    .append(r.getStockCount() == null ? "" : r.getStockCount()).append(',')
                    .append(escape(r.getProductUrl()))
                    .append('\n');
        }
        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
