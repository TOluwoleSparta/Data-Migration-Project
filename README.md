# Data Migration Project

This project features the transfer of data about a set of 10,000 employees from a .csv file to a database.

The following information about each employee is stored
- Employee ID
- Honorific/Title
- First Name
- Middle Initial
- Last Name
- Gender
- Email Address
- Date of Birth
- Work Start Date
- Salary

The program checks for the correctness of each data record, ensuring that the correct amount of data is given and that each piece of data is of the correct type and of valid format. Invalid records are written to a file called "Corrupted Data Records". The program then starts uploading all the valid records to the database. It also ensures that no records are duplicated; duplicate records are written to another file called "Duplicate Data Records". The files for the invalid and duplicate data records are written so that the owner of the .csv file may check their data and make changes as they see appropriate.

At the end of the upload, the program prints out
- How much time was spent reading the .csv file
- How much time was spent uploading to the database
- How much time was spent in total
