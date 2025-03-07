-- Insert mock users
INSERT INTO app_user (first_name, last_name, username, email, oauth_provider, oauth_provider_id) VALUES
('Alice', 'Johnson', 'alicej', 'alice@example.com', 'google', 'google-1234'),
('Bob', 'Smith', 'bobsmith', 'bob@example.com', 'google', 'google-5678'),
('Charlie', 'Brown', 'charlieb', 'charlie@example.com', 'github', 'github-91011');

-- Insert refresh tokens
INSERT INTO refresh_tokens (user_id, token, expiry) VALUES
(1, 'mock_refresh_token_1', NOW() + INTERVAL '7 days'),
(2, 'mock_refresh_token_2', NOW() + INTERVAL '7 days');

-- Insert roles
INSERT INTO app_role (name, description) VALUES
('USER', 'Regular user with basic privileges'),
('ADMIN', 'Administrator with full access');

-- Assign roles to users
INSERT INTO app_user_role_mapping (user_id, role_id) VALUES
(1, 1), -- Alice is a USER
(2, 1), -- Bob is a USER
(3, 2); -- Charlie is an ADMIN

-- Insert statuses
INSERT INTO status_type (name, description) VALUES
('Active', 'User is active'),
('Suspended', 'User account is suspended');

-- Assign statuses to users
INSERT INTO app_user_status (user_id, status_type_id) VALUES
(1, 1), -- Alice is active
(2, 1), -- Bob is active
(3, 2); -- Charlie is suspended

-- Insert profiles (one-to-one with users)
INSERT INTO profile (user_id) VALUES
(1), (2), (3);

-- Insert allergies
INSERT INTO allergy (name, description) VALUES
('Peanuts', 'Allergy to peanuts'),
('Gluten', 'Gluten intolerance'),
('Lactose', 'Lactose intolerance');

-- Assign allergies to profiles
INSERT INTO profile_allergy (profile_id, allergy_id) VALUES
(1, 1), -- Alice is allergic to peanuts
(2, 2), -- Bob has gluten intolerance
(3, 3); -- Charlie is lactose intolerant

-- Insert flavors
INSERT INTO flavor (name, description) VALUES
('Sweet', 'Preference for sweet flavors'),
('Spicy', 'Preference for spicy food'),
('Sour', 'Preference for sour flavors');

-- Insert profile flavor preferences
INSERT INTO profile_flavor_preference (profile_id, flavor_id, preference_level) VALUES
(1, 1, 8), -- Alice likes sweet (8/10)
(2, 2, 10), -- Bob loves spicy (10/10)
(3, 3, 5); -- Charlie is neutral about sour (5/10)

-- Insert dishes
INSERT INTO dish (name, description) VALUES
('Pizza', 'Classic Italian pizza with cheese and tomato'),
('Sushi', 'Japanese rice rolls with fish and vegetables'),
('Pasta', 'Pasta with creamy sauce');

-- Insert dish history (which dishes a user has eaten and rated)
INSERT INTO dish_history (profile_id, dish_id, user_rating) VALUES
(1, 1, 5), -- Alice rated Pizza 5/5
(2, 2, 4), -- Bob rated Sushi 4/5
(3, 3, 3); -- Charlie rated Pasta 3/5

-- Insert constraint types
INSERT INTO constraint_type (name) VALUES
('Vegetarian'), ('Vegan'), ('No Pork');

-- Assign constraints to profiles
INSERT INTO profile_constraint (profile_id, constraint_type_id) VALUES
(1, 1), -- Alice is vegetarian
(2, 2), -- Bob is vegan
(3, 3); -- Charlie does not eat pork

-- Insert special preferences
INSERT INTO special_preference (profile_id, description) VALUES
(1, 'Prefers organic food'),
(2, 'Loves high-protein meals'),
(3, 'Avoids processed sugar');

-- Index Optimization
ANALYZE;
