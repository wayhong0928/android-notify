DROP DATABASE IF EXISTS android_notify_system;
CREATE DATABASE android_notify_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE android_notify_system;

-- 會員
CREATE TABLE Members (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Account VARCHAR(50) UNIQUE NOT NULL,
    Username VARCHAR(50) NOT NULL,
    Password VARCHAR(100) NOT NULL
);

-- 公告類別
CREATE TABLE Categories (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(50) NOT NULL
);

-- 訂閱類別
CREATE TABLE SubscriptionCategories (
    MemberID INT,
    CategoryID INT,
    PRIMARY KEY (MemberID, CategoryID),
    FOREIGN KEY (MemberID) REFERENCES Members(ID),
    FOREIGN KEY (CategoryID) REFERENCES Categories(ID)
);

-- 六個公告類別
INSERT INTO Categories (Name) VALUES
('行政公告'), ('徵才公告'), ('校內徵才'), ('校外來文'), ('實習/就業'), ('活動預告');

-- 會員資料
INSERT INTO Members (Account, Username, Password)
VALUES
('user1', 'User One', 'password1'),
('user2', 'User Two', 'password2'),
('user3', 'User Three', 'password3');

-- 隨機訂閱資料，每位使用者至少訂閱一個公告類別
INSERT INTO SubscriptionCategories (MemberID, CategoryID)
VALUES (1, 2), (1, 4), (1, 5), (2, 1), (2, 2), (3, 2);

SELECT * FROM Members;
SELECT * FROM Categories;
SELECT * FROM SubscriptionCategories;


DELIMITER //

-- 根據account查詢使用者ID
CREATE PROCEDURE GetUserIdFromAccount(IN user_account VARCHAR(255), OUT user_id INT)
BEGIN
    SELECT ID INTO user_id FROM Members WHERE Members.Account = user_account;
    SELECT user_id;
END //

-- 使用user_id查詢使用者訂閱了哪些類別
CREATE PROCEDURE GetUserSubscribedCategories(IN user_id INT)
BEGIN
    SELECT Categories.ID FROM Categories
    INNER JOIN SubscriptionCategories ON Categories.ID = SubscriptionCategories.CategoryID
    INNER JOIN Members ON Members.ID = SubscriptionCategories.MemberID
    WHERE Members.ID = user_id;
END //

DELIMITER ;

-- SET @user_id = 0; -- 初始化 user_id 變數
-- CALL GetUserIdFromAccount('user2', @user_id);
-- SELECT @user_id; -- 檢查獲得的 user_id
-- CALL GetUserSubscribedCategories(@user_id);
