-------------------------------------
-- CreateDisturbanceTest.test2
-------------------------------------
INSERT INTO disturbance.feedback(created, party_id)
VALUES('2021-11-21 10:05:48.198', 'fbfbd90c-4c47-11ec-81d3-0242ac130003');
INSERT INTO disturbance.feedback(created, party_id)
VALUES('2021-11-23 12:05:48.198', '257f6aa0-4c48-11ec-81d3-0242ac130003');

-------------------------------------
-- ReadDisturbanceTest.test1
-- DisturbanceRepositoryTest.*
-------------------------------------
INSERT INTO disturbance.disturbance(id, category, description, disturbance_id, planned_start_date, planned_stop_date, status, title, deleted, created)
VALUES(2, 'COMMUNICATION', 'Description', 'disturbance-2', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0, '2021-09-23 09:05:48.198');

INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('0d64beb2-3aea-11ec-8d3d-0242ac130003', 'Streetname 11', 2);
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('0d64c132-3aea-11ec-8d3d-0242ac130003', 'Streetname 22', 2);
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('0d64c42a-3aea-11ec-8d3d-0242ac130003', 'Streetname 33', 2);

-------------------------------------
-- ReadDisturbanceTest.test2
-- ReadDisturbanceTest.test3
-- ReadDisturbanceTest.test4
-------------------------------------
INSERT INTO disturbance.disturbance(id, category, description, disturbance_id, planned_start_date, planned_stop_date, status, title, deleted)
VALUES(3, 'ELECTRICITY', 'Description', 'disturbance-3', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0);
INSERT INTO disturbance.disturbance(id, category, description, disturbance_id, planned_start_date, planned_stop_date, status, title, deleted)
VALUES(4, 'COMMUNICATION', 'Description', 'disturbance-4', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'CLOSED', 'Title', 0);

INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('c76ae496-3aed-11ec-8d3d-0242ac130003', 'Streetname 11', 3);
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('c76ae496-3aed-11ec-8d3d-0242ac130003', 'Streetname 11', 4);

-------------------------------------
-- UpdateDisturbanceTest.test1
-- DisturbanceFeedbackRepositoryTest.*
-------------------------------------
INSERT INTO disturbance.disturbance(id, category, description, disturbance_id, planned_start_date, planned_stop_date, status, title, deleted)
VALUES(5, 'ELECTRICITY', 'Description', 'disturbance-5', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0);

INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-1', 'Streetname 11', 5); -- will have feedback
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-2', 'Streetname 22', 5);
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-3', 'Streetname 33', 5); -- will have feedback

INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-5', 'affected-1');
INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-5', 'affected-3');

-------------------------------------
-- UpdateDisturbanceTest.test2
-------------------------------------
INSERT INTO disturbance.disturbance(id, category, description, disturbance_id, planned_start_date, planned_stop_date, status, title, deleted)
VALUES(6, 'ELECTRICITY', 'Description', 'disturbance-6', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0);

INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-1', 'Streetname 11', 6); -- will have feedback
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-2', 'Streetname 22', 6);
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('0d64c42a-3aea-11ec-8d3d-0242ac130003', 'Streetname 33', 6); -- will have feedback

INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-6', 'affected-1');
INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-6', '0d64c42a-3aea-11ec-8d3d-0242ac130003');

-------------------------------------
-- UpdateDisturbanceTest.test3
-- DisturbanceFeedbackRepositoryTest.*
-------------------------------------
INSERT INTO disturbance.disturbance(id, category, description, disturbance_id, planned_start_date, planned_stop_date, status, title, deleted)
VALUES(7, 'ELECTRICITY', 'Description', 'disturbance-7', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0);

INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('eeca0a46-3b1d-11ec-8d3d-0242ac130003', 'Streetname 11', 7); -- will have feedback
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('eeca0c8a-3b1d-11ec-8d3d-0242ac130003', 'Streetname 22', 7); -- will have feedback
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('eeca0d7a-3b1d-11ec-8d3d-0242ac130003', 'Streetname 33', 7); -- will have feedback

INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-7', 'eeca0a46-3b1d-11ec-8d3d-0242ac130003');
INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-7', 'eeca0c8a-3b1d-11ec-8d3d-0242ac130003');
INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-7', 'eeca0d7a-3b1d-11ec-8d3d-0242ac130003');

-------------------------------------
-- UpdateDisturbanceTest.test4
-------------------------------------
INSERT INTO disturbance.disturbance(id, category, description, disturbance_id, planned_start_date, planned_stop_date, status, title, deleted)
VALUES(8, 'ELECTRICITY', 'Description', 'disturbance-8', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0);

INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-1', 'Streetname 11', 8); -- will have feedback
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-2', 'Streetname 22', 8);
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-3', 'Streetname 33', 8); -- will have feedback

INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-8', 'affected-1');
INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-8', 'affected-3');

-------------------------------------
-- UpdateDisturbanceTest.test5
-------------------------------------
INSERT INTO disturbance.disturbance(id, category, description, disturbance_id, planned_start_date, planned_stop_date, status, title, deleted)
VALUES(12, 'ELECTRICITY', 'Description', 'disturbance-12', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'PLANNED', 'Title', 0);

INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-1', 'Streetname 11', 12); -- will have feedback
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-2', 'Streetname 22', 12);
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-3', 'Streetname 33', 12); -- will have feedback

INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-12', 'affected-1');
INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-12', 'affected-3');

-------------------------------------
-- DeleteDisturbanceTest.test1
-------------------------------------
INSERT INTO disturbance.disturbance(id, category, description, disturbance_id, planned_start_date, planned_stop_date, status, title, deleted)
VALUES(9, 'ELECTRICITY', 'Description', 'disturbance-9', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0);

INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-1', 'Streetname 11', 9);
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-2', 'Streetname 22', 9); -- will have feedback
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-3', 'Streetname 33', 9); -- will have feedback

INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-9', 'affected-2');
INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-9', 'affected-3');

-------------------------------------
-- DeleteDisturbanceTest.test2
-------------------------------------
INSERT INTO disturbance.disturbance(id, category, description, disturbance_id, planned_start_date, planned_stop_date, status, title, deleted)
VALUES(10, 'ELECTRICITY', 'Description', 'disturbance-10', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'CLOSED', 'Title', 0);

INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-1', 'Streetname 11', 9);
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-2', 'Streetname 22', 9); -- will have feedback
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-3', 'Streetname 33', 9); -- will have feedback

INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-10', 'affected-2');
INSERT INTO disturbance.disturbance_feedback(category,  disturbance_id, party_id)
VALUES('ELECTRICITY', 'disturbance-10', 'affected-3');

-------------------------------------
-- CreateDisturbanceFeedbackTest.test1
-------------------------------------
INSERT INTO disturbance.disturbance(id, category, description, disturbance_id, planned_start_date, planned_stop_date, status, title, deleted)
VALUES(11, 'COMMUNICATION', 'Description', 'disturbance-11', '2021-12-31 11:30:45', '2022-01-11 11:30:45', 'OPEN', 'Title', 0);

INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-1', 'Streetname 11', 11);
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-2', 'Streetname 22', 11);
INSERT INTO disturbance.affected (party_id, reference, parent_id) 
VALUES('affected-3', 'Streetname 33', 11);

-------------------------------------
-- DeleteFeedbackTest.test1
-- FeedbackRepositoryTest.*
-------------------------------------
INSERT INTO disturbance.feedback(created, party_id)
VALUES('2021-11-23 10:05:48.198', '3c1236ca-4c44-11ec-81d3-0242ac130003');
INSERT INTO disturbance.feedback(created, party_id)
VALUES('2021-12-28 12:20:41.298', '49a974ea-9137-419b-bcb9-ad74c81a1d7f');

