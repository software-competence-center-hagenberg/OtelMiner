--select lo_get("json") from span s where s.trace_id = '56bf4e52c33fdea04387db466c2f2e9f';
delete from "declare" where prob_declare_id = '9dda37f9-bed1-4f55-b873-60ac1ec13792';
delete from prob_declare_to_trace where prob_declare_id = '9dda37f9-bed1-4f55-b873-60ac1ec13792';
delete from prob_declare where id = '9dda37f9-bed1-4f55-b873-60ac1ec13792';
ALTER TABLE public."declare" 
ALTER COLUMN constraint_template TYPE varchar(500);

---------------------------------------------------------------------------------
SELECT 
    COUNT(DISTINCT t.id) AS nr_traces,
    COUNT(s.id) AS nr_spans,
    MIN(t.nr_nodes) AS min_nr_spans,
    MAX(t.nr_nodes) AS max_nr_spans
FROM 
    trace t
LEFT JOIN 
    span s ON s.trace_id = t.id
WHERE 
    t.trace_data_type = 'DYNATRACE_SPANS_LIST';

---------------------------------------------------------------------------------

SELECT 
    FLOOR(EXTRACT(EPOCH FROM (update_date - insert_date)) / 60) AS difference_minutes,
    FLOOR(EXTRACT(EPOCH FROM (update_date - insert_date)) % 60) AS difference_seconds,
    FLOOR((EXTRACT(EPOCH FROM (update_date - insert_date)) * 1000) % 1000) AS difference_milliseconds
from prob_declare where id = 'c9dc89a7-b746-4845-bdb3-e11099d1f4e7';

---------------------------------------------------------------------------------
select count(*) from declare where prob_declare_id = 'c9dc89a7-b746-4845-bdb3-e11099d1f4e7';

---------------------------------------------------------------------------------
SELECT 
    CASE 
        WHEN probability < 0.1 THEN '< 0.1'
        WHEN probability >= 0.1 AND probability < 0.2 THEN '0.1 - 0.2'
        WHEN probability >= 0.2 AND probability < 0.3 THEN '0.2 - 0.3'
        WHEN probability >= 0.3 AND probability < 0.4 THEN '0.3 - 0.4'
        WHEN probability >= 0.4 AND probability < 0.5 THEN '0.4 - 0.5'
        WHEN probability >= 0.5 AND probability < 0.6 THEN '0.5 - 0.6'
        WHEN probability >= 0.6 AND probability < 0.7 THEN '0.6 - 0.7'
        WHEN probability >= 0.7 AND probability < 0.8 THEN '0.7 - 0.8'
        WHEN probability >= 0.8 AND probability < 0.9 THEN '0.8 - 0.9'
        WHEN probability >= 0.9 AND probability < 1 THEN '0.9 - 1'
        WHEN probability = 1 THEN '= 1'
    END AS probability_range,
    COUNT(*) AS constraint_count
FROM 
    public."declare"
WHERE 
    prob_declare_id = 'c9dc89a7-b746-4845-bdb3-e11099d1f4e7'
GROUP BY 
    probability_range
ORDER BY 
    MIN(probability);

---------------------------------------------------------------------------------
SELECT 
    CASE 
        WHEN constraint_template LIKE 'EXISTENCE%' THEN 'EXISTENCE'
        WHEN constraint_template LIKE 'INIT%' THEN 'INIT'
        WHEN constraint_template LIKE 'LAST%' THEN 'LAST'
        WHEN constraint_template LIKE 'CHOICE%' THEN 'CHOICE'
        WHEN constraint_template LIKE 'RESPONSE%' THEN 'RESPONSE'
        WHEN constraint_template LIKE 'PRECEDENCE%' THEN 'PRECEDENCE'
        WHEN constraint_template LIKE 'SUCCESSION%' THEN 'SUCCESSION'
        WHEN constraint_template LIKE 'ALTERNATE_RESPONSE%' THEN 'ALTERNATE_RESPONSE'
        WHEN constraint_template LIKE 'ALTERNATE_PRECEDENCE%' THEN 'ALTERNATE_PRECEDENCE'
        WHEN constraint_template LIKE 'ALTERNATE_SUCCESSION%' THEN 'ALTERNATE_SUCCESSION'
        WHEN constraint_template LIKE 'CHAIN_RESPONSE%' THEN 'CHAIN_RESPONSE'
        WHEN constraint_template LIKE 'CHAIN_PRECEDENCE%' THEN 'CHAIN_PRECEDENCE'
        WHEN constraint_template LIKE 'CHAIN_SUCCESSION%' THEN 'CHAIN_SUCCESSION'
    END AS constraint_template_group,
    SUM(nr) AS nr_total,
    ROUND(SUM(probability)::numeric, 4) AS probability_total,
    ROUND(AVG(probability)::numeric, 4) AS average_probability
FROM 
    public."declare"
WHERE 
    constraint_template LIKE 'EXISTENCE%' OR
    constraint_template LIKE 'INIT%' OR
    constraint_template LIKE 'LAST%' OR
    constraint_template LIKE 'CHOICE%' OR
    constraint_template LIKE 'RESPONSE%' OR
    constraint_template LIKE 'PRECEDENCE%' OR
    constraint_template LIKE 'SUCCESSION%' OR
    constraint_template LIKE 'ALTERNATE_RESPONSE%' OR
    constraint_template LIKE 'ALTERNATE_PRECEDENCE%' OR
    constraint_template LIKE 'ALTERNATE_SUCCESSION%' OR
    constraint_template LIKE 'CHAIN_RESPONSE%' OR
    constraint_template LIKE 'CHAIN_PRECEDENCE%' OR
    constraint_template LIKE 'CHAIN_SUCCESSION%'
    AND prob_declare_id = 'c9dc89a7-b746-4845-bdb3-e11099d1f4e7'
GROUP BY 
    constraint_template_group
ORDER BY 
    constraint_template_group;

---------------------------------------------------------------------------------

--select * from prob_declare pd where pd.id = '79e82494-14f1-412a-ba39-b8264b5bf135';

--select * from declare d where d.prob_declare_id = '79e82494-14f1-412a-ba39-b8264b5bf135';

--DELETE FROM prob_declare_to_trace;
--DELETE FROM prob_declare ;