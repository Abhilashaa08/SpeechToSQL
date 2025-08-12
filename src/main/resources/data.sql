INSERT INTO customers(name) VALUES ('Nelson'),('Alice'),('Rita'),('Sam');

INSERT INTO orders(customer_id,status,amount,created_at) VALUES
(1,'completed',120.50, DATEADD('DAY', -1, CURRENT_DATE())),
(1,'pending',  90.00, DATEADD('DAY', -2, CURRENT_DATE())),
(2,'completed',200.00, DATEADD('DAY', -10, CURRENT_DATE())),
(2,'cancelled', 50.00, DATEADD('DAY', -15, CURRENT_DATE())),
(3,'completed',350.00, DATEADD('DAY', -25, CURRENT_DATE())),
(4,'completed', 75.00, DATEADD('DAY', -5, CURRENT_DATE())),
(1,'completed',180.00, CURRENT_DATE());
