-- V1__Initial_schema.sql

-- Создание таблицы городов
CREATE TABLE cities (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL UNIQUE
);

-- Создание таблицы пользователей
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(50) NOT NULL UNIQUE,
                       password_hash VARCHAR(225) NOT NULL,
                       full_name VARCHAR(30) NOT NULL,
                       phone_number VARCHAR(15) UNIQUE,
                       city_id INTEGER REFERENCES cities(id),
                       registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       avatar_url VARCHAR(512)
);
CREATE INDEX idx_users_email ON users(email);

-- Создание таблицы ролей
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE
);

-- Начальное заполнение таблицы ролей
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- Создание связующей таблицы для пользователей и ролей
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

-- Создание таблицы категорий
CREATE TABLE categories (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            parent_category_id INTEGER REFERENCES categories(id)
);

INSERT INTO categories (name) VALUES ('Электроника'), ('Одежда'), ('Транспорт');

-- Создание таблицы объявлений
CREATE TABLE advertisements (
                                id BIGSERIAL PRIMARY KEY,
                                title VARCHAR(25) NOT NULL,
                                description TEXT,
                                price NUMERIC(12, 2) NOT NULL,
                                status VARCHAR(50) NOT NULL,
                                condition VARCHAR(50),
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                seller_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                category_id INTEGER NOT NULL REFERENCES categories(id),
                                city_id INTEGER NOT NULL REFERENCES cities(id)
);
CREATE INDEX idx_advertisements_status ON advertisements(status);
CREATE INDEX idx_advertisements_seller_id ON advertisements(seller_id);

-- Создание таблицы изображений для объявлений
CREATE TABLE advertisement_images (
                                      id BIGSERIAL PRIMARY KEY,
                                      advertisement_id BIGINT NOT NULL REFERENCES advertisements(id) ON DELETE CASCADE,
                                      image_url VARCHAR(512) NOT NULL,
                                      is_preview BOOLEAN NOT NULL DEFAULT FALSE,
                                      uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_advertisement_images_advertisement_id ON advertisement_images(advertisement_id);