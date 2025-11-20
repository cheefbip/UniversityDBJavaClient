/*
TABLES
*/

CREATE TABLE Departments (
	DeptNo INT PRIMARY KEY,
	Dept_Name CHAR(20),
	Main_Office INT,
	ChairSSN CHAR(9)
);

CREATE TABLE Professors (
	SSN CHAR(9) PRIMARY KEY,
	Name TEXT,
	Gender VARCHAR(1),
	Age INT,
	Rank INT,
	Research_Spec CHAR(20),
    DeptNo INT,
    FOREIGN KEY (DeptNo) REFERENCES Departments (DeptNo)
                    ON DELETE RESTRICT
);

-- Departments Chairman SSN Foreign Key -> Professor SSN
ALTER TABLE DEPARTMENTS
ADD FOREIGN KEY (ChairSSN) REFERENCES Professors (SSN)
					ON DELETE RESTRICT;

CREATE TABLE Projects (
	ProjectNo INT PRIMARY KEY,
	Spons_name CHAR(20),
	Start_date DATE,
	End_date DATE,
	Budget Decimal(10,2),
    Principal_Investigator CHAR(9),
	Foreign Key (Principal_Investigator) REFERENCES Professors (SSN)
						ON DELETE RESTRICT
);


CREATE TABLE Grad_Students (
	SSN CHAR(9) PRIMARY KEY,
	Name TEXT,
	Age INT,
	Gender VARCHAR(1),
	Degree_Program CHAR(20),
    Advisor_SSN CHAR(9),
    DeptNo INT,
	FOREIGN KEY (Advisor_SSN) REFERENCES Grad_Students (SSN)
						ON DELETE RESTRICT,
	FOREIGN KEY (DeptNo) REFERENCES Departments (DeptNo)
						ON DELETE RESTRICT
);


CREATE TABLE Co_Investigators (
	SSN CHAR(9),
    ProjectNo INT,
    PRIMARY KEY (SSN, ProjectNo),
    FOREIGN KEY (SSN) REFERENCES Professors (SSN)
                    ON DELETE RESTRICT,
	FOREIGN KEY (ProjectNo) REFERENCES Projects (ProjectNo)
					ON DELETE RESTRICT
);


CREATE TABLE Research_Assistants (
	SSN CHAR(9) PRIMARY KEY,
	ProjectNo INT,
	FOREIGN KEY (ProjectNo) REFERENCES Projects (ProjectNo)
					ON DELETE RESTRICT
);



/*
	FUNCTIONS
*/

-- Function: female_faculty 
CREATE FUNCTION female_faculty () RETURNS INT AS $$
    DECLARE
        num_total INT;
        num_female INT;
    BEGIN
        SELECT COUNT(*) INTO num_total FROM Professors;
        
        SELECT COUNT(*) INTO num_female FROM Professors
            WHERE Gender = 'F';
        RETURN num_female::NUMERIC/num_total * 100;
    END;
$$ LANGUAGE 'plpgsql';

-- Function: total_people 
CREATE FUNCTION total_people (pno INT) RETURNS INT AS $$
    DECLARE
        num_pi INT;
        num_copi INT;
        num_students INT;
    BEGIN
		SELECT COUNT(ProjectNo) INTO num_pi FROM Projects
			WHERE ProjectNo = pno;

        SELECT COUNT(*) INTO num_copi FROM Co_Investigators
			WHERE ProjectNo = pno;
        
        SELECT COUNT(*) INTO num_students FROM Research_Assistants
			WHERE ProjectNo = pno;
        RETURN num_pi + num_copi + num_students;
    END;
$$ LANGUAGE 'plpgsql';



/*
	TRIGGERS
*/

-- Function: faculty_restrict 
CREATE FUNCTION faculty_restrict_fn()
RETURNS TRIGGER AS $$
DECLARE
    num_copi INT;
BEGIN
    SELECT COUNT(*) INTO num_copi FROM Co_Investigators
    	WHERE ProjectNo = NEW.ProjectNo;

    IF num_copi >= 4 THEN
        RAISE EXCEPTION 'Project co-investigators cannot exceed 4.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

-- Trigger: faculty_restrict
CREATE TRIGGER faculty_restrict
	BEFORE INSERT ON Co_Investigators
	FOR EACH ROW EXECUTE PROCEDURE faculty_restrict_fn();

-- Function: student_restrict 
CREATE FUNCTION student_restrict_fn()
RETURNS TRIGGER AS $$
DECLARE
    num_projects INT;
BEGIN
    SELECT COUNT(*) INTO num_projects FROM Research_Assistants
    	WHERE SSN = NEW.SSN;

    IF num_projects >= 2 THEN
        RAISE EXCEPTION 'Students cannot work on more than 2 projects.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

-- Trigger: student_restrict 
CREATE TRIGGER student_restrict
	BEFORE INSERT ON Research_Assistants
	FOR EACH ROW EXECUTE PROCEDURE student_restrict_fn();