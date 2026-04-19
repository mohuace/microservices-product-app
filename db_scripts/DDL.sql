CREATE TABLE Product (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);


CREATE TABLE orders (
    id INT IDENTITY(1,1) PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL
);


CREATE TABLE order_item (
    id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,

    CONSTRAINT FK_order_item_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
        ON DELETE CASCADE
);


CREATE TABLE Inventory (
    product_id INT IDENTITY(1,1) PRIMARY KEY,
    quantity INT NOT NULL
);


