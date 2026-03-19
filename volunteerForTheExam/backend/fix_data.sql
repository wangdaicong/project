USE volunteer_exam;

UPDATE university 
SET introduction = '学校简介待完善',
    features = '办学特色待完善',
    address = '学校地址待完善',
    phone = '010-00000000'
WHERE introduction IS NULL OR introduction = '';

SELECT COUNT(*) as updated FROM university WHERE introduction IS NOT NULL;
