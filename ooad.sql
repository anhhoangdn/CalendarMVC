CREATE DATABASE IF NOT EXISTS ooad;
USE ooad;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS appointment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    location VARCHAR(200),
    meeting_date DATE NOT NULL,
    start_hour INT NOT NULL,
    end_hour INT NOT NULL,
    type_appointment VARCHAR(20) DEFAULT 'Đơn'
);

CREATE TABLE IF NOT EXISTS take (
    user_id INT NOT NULL,
    appointment_id INT NOT NULL,
    PRIMARY KEY (user_id, appointment_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reminder (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS take_rmd (
    appointment_id INT NOT NULL,
    reminder_id INT NOT NULL,
    PRIMARY KEY (appointment_id, reminder_id),
    FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE,
    FOREIGN KEY (reminder_id) REFERENCES reminder(id) ON DELETE CASCADE
);

INSERT INTO users (name, phone_number) VALUES
('Nguyễn Văn An',   '0901234567'),
('Trần Thị Bình',   '0912345678'),
('Lê Văn Cường',    '0923456789'),
('Phạm Thị Dung',   '0934567890'),
('Hoàng Văn Em',    '0945678901');

INSERT INTO reminder (title) VALUES
('Nhắc trước 15 phút'),
('Nhắc trước 30 phút'),
('Nhắc trước 1 ngày');

INSERT INTO appointment (name, location, meeting_date, start_hour, end_hour, type_appointment) VALUES
('Họp nhóm dự án OOAD',      'Phòng A101',          '2026-04-30', 8,  10, 'Nhóm'),
('Gặp khách hàng ABC',       'Văn phòng tầng 3',    '2026-04-30', 14, 16, 'Đơn'),
('Review code sprint 5',     'Phòng Lab CNTT',      '2026-05-01', 9,  11, 'Nhóm'),
('Báo cáo tiến độ tuần',     'Phòng họp B205',      '2026-05-02', 13, 14, 'Nhóm'),
('Tư vấn khách hàng XYZ',    'Coffee Highlands',    '2026-05-03', 10, 12, 'Đơn'),
('Họp ban lãnh đạo',         'Phòng VIP tầng 5',    '2026-05-05', 8,  10, 'Nhóm'),
('Demo sản phẩm',            'Hội trường lớn',      '2026-05-06', 14, 17, 'Nhóm'),
('Phỏng vấn ứng viên',       'Phòng HR',            '2026-05-07', 9,  10, 'Đơn'),
('Seminar công nghệ AI',     'Giảng đường G1',      '2026-05-08', 13, 17, 'Nhóm'),
('Ký hợp đồng dự án mới',   'Văn phòng công chứng','2026-05-09', 10, 11, 'Đơn');

INSERT INTO take (user_id, appointment_id) VALUES
(1, 1),(1, 2),(1, 3),(1, 4),(1, 5),
(1, 6),(1, 7),(1, 8),(1, 9),(1, 10),
(2, 1),(2, 3),(2, 4),(2, 6),(2, 7),(2, 9),
(3, 1),(3, 3),(3, 6),(3, 7),(3, 9),
(4, 4),(4, 6),(4, 7),(4, 9),
(5, 1),(5, 7),(5, 9);

INSERT INTO take_rmd (appointment_id, reminder_id) VALUES
(1, 1),(1, 3),
(2, 2),
(3, 1),(3, 2),
(6, 3),
(7, 1),(7, 3),
(9, 2),(9, 3);