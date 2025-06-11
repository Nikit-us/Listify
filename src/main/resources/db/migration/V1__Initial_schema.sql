-- V1__Initial_schema.sql
DROP TABLE IF EXISTS advertisement_images, advertisements, user_roles, users, categories, cities, districts, regions, roles CASCADE;

-- === СОЗДАНИЕ НОВОЙ СТРУКТРУРЫ ТАБЛИЦ ===

-- Создание таблицы областей
CREATE TABLE regions (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(100) NOT NULL UNIQUE
);

-- Создание таблицы районов
CREATE TABLE districts (
                           id SERIAL PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           region_id INTEGER NOT NULL REFERENCES regions(id) ON DELETE CASCADE,
                           UNIQUE (name, region_id)
);

-- Обновленная таблица городов с привязкой к району
CREATE TABLE cities (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        district_id INTEGER NOT NULL REFERENCES districts(id) ON DELETE CASCADE,
                        UNIQUE (name, district_id)
);

-- Создание таблицы ролей
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE
);

-- Создание таблицы пользователей
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(225) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       phone_number VARCHAR(50) UNIQUE,
                       city_id INTEGER REFERENCES cities(id),
                       registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       avatar_url VARCHAR(512)
);
CREATE INDEX idx_users_email ON users(email);

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
                            parent_category_id INTEGER REFERENCES categories(id) ON DELETE CASCADE
);

-- Создание таблицы объявлений
CREATE TABLE advertisements (
                                id BIGSERIAL PRIMARY KEY,
                                title VARCHAR(50) NOT NULL,
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

-- === НАПОЛНЕНИЕ ТАБЛИЦ ДАННЫМИ ===

-- 1. Заполнение ролей
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- 2. Заполнение областей Беларуси
INSERT INTO regions (id, name) VALUES
                                   (1, 'Брестская область'),
                                   (2, 'Витебская область'),
                                   (3, 'Гомельская область'),
                                   (4, 'Гродненская область'),
                                   (5, 'Минская область'),
                                   (6, 'Могилёвская область'),
                                   (7, 'г. Минск');

-- 3. Заполнение районов и городов
-- Брестская область
INSERT INTO districts (id, name, region_id) VALUES (1, 'Брестский район', 1);
INSERT INTO cities (name, district_id) VALUES ('Брест', 1);
INSERT INTO districts (id, name, region_id) VALUES (2, 'Барановичский район', 1);
INSERT INTO cities (name, district_id) VALUES ('Барановичи', 2);
INSERT INTO districts (id, name, region_id) VALUES (3, 'Пинский район', 1);
INSERT INTO cities (name, district_id) VALUES ('Пинск', 3);
INSERT INTO districts (id, name, region_id) VALUES (4, 'Кобринский район', 1);
INSERT INTO cities (name, district_id) VALUES ('Кобрин', 4);
INSERT INTO districts (id, name, region_id) VALUES (5, 'Березовский район', 1);
INSERT INTO cities (name, district_id) VALUES ('Берёза', 5);

-- Витебская область
INSERT INTO districts (id, name, region_id) VALUES (6, 'Витебский район', 2); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Витебск', 6);
INSERT INTO districts (id, name, region_id) VALUES (7, 'Оршанский район', 2); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Орша', 7);
INSERT INTO districts (id, name, region_id) VALUES (8, 'Новополоцкий горисполком', 2); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Новополоцк', 8);
INSERT INTO districts (id, name, region_id) VALUES (9, 'Полоцкий район', 2); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Полоцк', 9);

-- Гомельская область
INSERT INTO districts (id, name, region_id) VALUES (10, 'Гомельский район', 3); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Гомель', 10);
INSERT INTO districts (id, name, region_id) VALUES (11, 'Мозырский район', 3); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Мозырь', 11);
INSERT INTO districts (id, name, region_id) VALUES (12, 'Жлобинский район', 3); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Жлобин', 12);
INSERT INTO districts (id, name, region_id) VALUES (13, 'Речицкий район', 3); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Речица', 13);

-- Гродненская область
INSERT INTO districts (id, name, region_id) VALUES (14, 'Гродненский район', 4); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Гродно', 14);
INSERT INTO districts (id, name, region_id) VALUES (15, 'Лидский район', 4); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Лида', 15);
INSERT INTO districts (id, name, region_id) VALUES (16, 'Слонимский район', 4); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Слоним', 16);
INSERT INTO districts (id, name, region_id) VALUES (17, 'Волковысский район', 4); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Волковыск', 17);

-- Минская область
INSERT INTO districts (id, name, region_id) VALUES (18, 'Минский район', 5); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Заславль', 18);
INSERT INTO districts (id, name, region_id) VALUES (19, 'Борисовский район', 5); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Борисов', 19);
INSERT INTO districts (id, name, region_id) VALUES (20, 'Солигорский район', 5); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Солигорск', 20);
INSERT INTO districts (id, name, region_id) VALUES (21, 'Молодечненский район', 5); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Молодечно', 21);

-- Могилёвская область
INSERT INTO districts (id, name, region_id) VALUES (22, 'Могилёвский район', 6); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Могилёв', 22);
INSERT INTO districts (id, name, region_id) VALUES (23, 'Бобруйский район', 6); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Бобруйск', 23);
INSERT INTO districts (id, name, region_id) VALUES (24, 'Осиповичский район', 6); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Осиповичи', 24);

-- г. Минск
INSERT INTO districts (id, name, region_id) VALUES (25, 'Центральный район', 7); -- ИСПРАВЛЕНО
INSERT INTO cities (name, district_id) VALUES ('Минск', 25);

-- 4. Заполнение категорий (иерархическая структура)
-- Родительские категории
INSERT INTO categories (id, name, parent_category_id) VALUES
                                                          (1, 'Транспорт', NULL),
                                                          (2, 'Недвижимость', NULL),
                                                          (3, 'Электроника', NULL),
                                                          (4, 'Дом и сад', NULL),
                                                          (5, 'Личные вещи', NULL),
                                                          (6, 'Хобби, отдых и спорт', NULL),
                                                          (7, 'Животные', NULL),
                                                          (8, 'Работа', NULL),
                                                          (9, 'Услуги', NULL);

-- Обновляем счетчик, чтобы избежать конфликта PRIMARY KEY
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));

-- Подкатегории для "Транспорт" (parent_id = 1)
INSERT INTO categories (name, parent_category_id) VALUES
                                                      ('Автомобили', 1),
                                                      ('Мотоциклы и мототехника', 1),
                                                      ('Запчасти и аксессуары', 1),
                                                      ('Водный транспорт', 1);

-- Подкатегории для "Недвижимость" (parent_id = 2)
INSERT INTO categories (name, parent_category_id) VALUES
                                                      ('Продажа квартир', 2),
                                                      ('Аренда квартир', 2),
                                                      ('Дома, дачи, коттеджи', 2),
                                                      ('Гаражи и машиноместа', 2);

-- Подкатегории для "Электроника" (parent_id = 3)
INSERT INTO categories (name, parent_category_id) VALUES
                                                      ('Телефоны и аксессуары', 3),
                                                      ('Компьютеры и ноутбуки', 3),
                                                      ('Фото- и видеотехника', 3),
                                                      ('ТВ, аудио, видео', 3);

-- Подкатегории для "Личные вещи" (parent_id = 5)
INSERT INTO categories (name, parent_category_id) VALUES
                                                      ('Одежда, обувь, аксессуары', 5),
                                                      ('Товары для детей и игрушки', 5),
                                                      ('Часы и украшения', 5);