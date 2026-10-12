package com.example.ecommerce.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repo.ProductRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductRepository products;
    public ProductController(ProductRepository products){this.products=products;}

    public record ProductResponse(Long id, String name, String description, BigDecimal price, Integer stock, String imageUrl) {}
    private ProductResponse dto(Product p){return new ProductResponse(p.getId(),p.getName(),p.getDescription(),p.getPrice(),p.getStock(),p.getImageData()==null?null:"/api/products/"+p.getId()+"/image");}

    @GetMapping("/products")
    public List<ProductResponse> all(){return products.findAll().stream().map(this::dto).toList();}
    @GetMapping("/products/{id}")
    public ProductResponse one(@PathVariable Long id){return dto(products.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)));}
    @GetMapping("/products/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        Product p=products.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        if(p.getImageData()==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No image");
        MediaType type;
        try { type=MediaType.parseMediaType(p.getImageContentType()==null?"application/octet-stream":p.getImageContentType()); }
        catch(Exception ex){type=MediaType.APPLICATION_OCTET_STREAM;}
        return ResponseEntity.ok().contentType(type).cacheControl(CacheControl.noCache()).body(p.getImageData());
    }

    @PostMapping(value="/admin/products", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@RequestParam String name, @RequestParam(required=false) String description,
      @RequestParam BigDecimal price, @RequestParam(defaultValue="0") Integer stock,
      @RequestPart(required=false) MultipartFile image) throws IOException {
        validate(name,price,stock);
        byte[] data=image==null||image.isEmpty()?null:image.getBytes();
        String type=image==null||image.isEmpty()?null:image.getContentType();
        if(data!=null && data.length>8*1024*1024) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"Image must be 8MB or smaller");
        if(type!=null && !type.startsWith("image/")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Upload an image file");
        return dto(products.save(new Product(name,description,price,stock,data,type)));
    }

    @PutMapping(value="/admin/products/{id}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse update(@PathVariable Long id, @RequestParam String name, @RequestParam(required=false) String description,
      @RequestParam BigDecimal price, @RequestParam(defaultValue="0") Integer stock,
      @RequestPart(required=false) MultipartFile image) throws IOException {
        validate(name,price,stock);
        Product p=products.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.setName(name);p.setDescription(description);p.setPrice(price);p.setStock(stock);
        if(image!=null&&!image.isEmpty()){
            if(image.getSize()>8*1024*1024) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"Image must be 8MB or smaller");
            if(image.getContentType()==null||!image.getContentType().startsWith("image/")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Upload an image file");
            p.setImageData(image.getBytes());p.setImageContentType(image.getContentType());
        }
        return dto(products.save(p));
    }

    @DeleteMapping("/admin/products/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){if(!products.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND);products.deleteById(id);}

    private void validate(String name, BigDecimal price, Integer stock){
        if(name==null||name.isBlank()||name.length()>160||price==null||price.signum()<0||stock==null||stock<0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Provide a product name, non-negative price and non-negative stock");
    }
}
