# API examples

## Register a customer
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{"username":"naveen","password":"Password@123"}
```

## List products
```http
GET http://localhost:8080/api/products
```

## Create a product with an image (admin)
```bash
curl -u admin:Admin@12345 -X POST http://localhost:8080/api/admin/products ^
  -F "name=Canvas Backpack" ^
  -F "description=Everyday backpack" ^
  -F "price=1499.00" ^
  -F "stock=15" ^
  -F "image=@C:\path\to\backpack.jpg"
```

## Add product to cart
```bash
curl -u naveen:Password@123 -H "Content-Type: application/json" ^
  -d "{\"productId\":1,\"quantity\":1}" ^
  http://localhost:8080/api/cart/items
```

## Notes on security
- HTTP Basic is used for simple local learning. Browsers encode credentials in Base64; this is not encryption. Use HTTPS in deployment.
- The frontend stores the Basic credential in localStorage for demo convenience. Do not copy this approach into a production application.
- The starter uses a username check in the UI to label the seeded admin account; backend authorization is enforced by Spring Security roles, not by this UI label.
