INSERT INTO Departments (DeptNo, Dept_Name, Main_Office, ChairSSN)
VALUES
(100, 'Computer Science', 'ET A322', NULL),
(200, 'Mathematics', 'ST F206', NULL),
(300, 'Biology', 'BIOS 143', NULL);



INSERT INTO Professors (SSN, Name, Gender, Age, Rank, Research_Spec, DeptNo)
VALUES
-- Computer Science
('000111111', 'Eun-Young Kang', 'F', 30, 1, 'Computer Graphics', 100),
('000222222', 'Chengyu Sun', 'M', 31, 2, 'Databases', 100),
('000333333', 'Navid Amini', 'M', 32, 3, 'Machine Learning', 100),
('000444444', 'Mark Baldwin', 'M', 35, 4, 'Assistive Technology', 100),
('000555555', 'Armando Beltran', 'M', 35, 5, 'AI', 100),
('000666666', 'Negin Forouzesh', 'F', 35, 6, 'Computational Bio', 100),
('000777777', 'Huiping Guo', 'F', 40, 7, 'Computer Networks', 100),
('000888888', 'Jiang Guo', 'M', 51, 8, 'Software Engineering', 100),
('000999999', 'Manveen Kaur', 'F', 32, 9, 'Smart Vehicles', 100),
-- Mathematics
('111111111', 'Euler', 'M', 27, 2, 'Number Theory', 200),
('111222222', 'Isaac Newton', 'M', 47, 1, 'Calculus', 200);
-- Biology
('222111111', 'Dr. Gregory House', 'M', 54, 1, 'Diagnostics', 300);



-- Set up the department chairmen
UPDATE Departments SET ChairSSN = '000111111' WHERE DeptNo = 100;
UPDATE Departments SET ChairSSN = '111222222' WHERE DeptNo = 200;
UPDATE Departments SET ChairSSN = '222111111' WHERE DeptNo = 300;



INSERT INTO Grad_Students (SSN, Name, Age, Gender, Degree_Program, DeptNo, Advisor_SSN)
VALUES
-- Computer Science
('111222333', 'John Doe', 24, 'M', 'MS', 100, '444555666'),
('222333444', 'Jane Doe', 27, 'F', 'PhD', 100, NULL),
('333444555', 'Joe', 26, 'M', 'MS', 100, '222333444'),
('444555666', 'Alyssa', 25, 'F', 'PhD', 100, '333444555'),
-- Mathematics
('555666777', 'Gauss', 26, 'M', 'PhD', 200, '666777888'),
('666777888', 'Archimedes', 32, 'M', 'PhD', 200, NULL);



INSERT INTO Projects (ProjectNo, Spons_Name, Start_Date, End_Date, Budget, Principal_Investigator)
VALUES
-- Computer Science
(1001, 'Lockheed Martin', '2024-01-01', '2025-01-01', 100000, '000222222'),
(1002, 'NASA', '2023-08-14', '2024-05-30', 750000, '000111111'),
(1003, 'Google', '2022-07-30', '2023-05-30', 750000, '000111111'),
(1004, 'Apple', '2023-08-14', '2024-05-30', 750000, '000111111'),
(1005, 'Boeing', '2020-01-01', '2021-12-01', 250000, '000222222'),
(1006, 'NSA', '2022-01-01', '2022-12-01', 400000, '000888888'),
-- Mathematics
(2001, 'NSF', '2021-01-01', '2021-12-01', 75000, '111222222');



INSERT INTO Co_Investigators (SSN, ProjectNo)
VALUES
-- Computer Science
('000333333', 1001),
('000444444', 1001),
('000555555', 1001),
('000888888', 1003),
('000666666', 1003),
('000444444', 1003),
('000333333', 1003),
('000777777', 1004),
('000777777', 1005);



INSERT INTO Research_Assistants (SSN, ProjectNo)
VALUES
-- Computer Science
('111222333', 1001),
('222333444', 1002),
('333444555', 1003),
('333444555', 1004),
('444555666', 1005),
('444555666', 1006),
-- Mathematics
('555666777', 2001),
('666777888', 2001);