DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
  id IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL
);

CREATE TABLE orders (
  id IDENTITY PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  status VARCHAR(50) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  created_at DATE NOT NULL,
  CONSTRAINT fk_cust FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_status ON orders(status);
