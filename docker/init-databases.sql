-- 각 마이크로서비스용 데이터베이스 생성
CREATE DATABASE user_db;
CREATE DATABASE product_db;
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE inventory_db;
CREATE DATABASE shipping_db;
CREATE DATABASE notification_db;
CREATE DATABASE event_logging_db;

-- 데이터베이스별 권한 부여
GRANT ALL PRIVILEGES ON DATABASE user_db TO orderflow;
GRANT ALL PRIVILEGES ON DATABASE product_db TO orderflow;
GRANT ALL PRIVILEGES ON DATABASE order_db TO orderflow;
GRANT ALL PRIVILEGES ON DATABASE payment_db TO orderflow;
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO orderflow;
GRANT ALL PRIVILEGES ON DATABASE shipping_db TO orderflow;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO orderflow;
GRANT ALL PRIVILEGES ON DATABASE event_logging_db TO orderflow;