
-- Drop the `public` schema and all its objects
DROP SCHEMA public CASCADE;
-- Recreate the `public` schema
CREATE SCHEMA public;
-- Restore default privileges
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;


-- App User Table (updated to store OAuth provider details)
CREATE TABLE app_user (
  id SERIAL PRIMARY KEY,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  username VARCHAR(255) UNIQUE,  -- Optional (OAuth might not provide this)
  email VARCHAR(255) UNIQUE NOT NULL,
  oauth_provider VARCHAR(50) NOT NULL,  -- ('google', 'github', 'facebook')
  oauth_provider_id VARCHAR(255) UNIQUE NOT NULL, -- Unique ID from OAuth provider
  created_at TIMESTAMP DEFAULT now()
);

-- Refresh Tokens Table
CREATE TABLE refresh_tokens (
  id SERIAL PRIMARY KEY,
  user_id INT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  token TEXT NOT NULL UNIQUE,
  expiry TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT now()
);

-- Role Table
CREATE TABLE app_role (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL, -- Ensuring role names are unique
  description TEXT,
  created_at TIMESTAMP DEFAULT now()
);

-- User Role Mapping (Many-to-Many Relationship)
CREATE TABLE app_user_role_mapping (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  role_id INTEGER NOT NULL REFERENCES app_role(id) ON DELETE CASCADE,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT now()
);

-- Status Type
CREATE TABLE status_type (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT now()
);

-- User Status
CREATE TABLE app_user_status (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  status_type_id INTEGER NOT NULL REFERENCES status_type(id) ON DELETE CASCADE,
  created_at TIMESTAMP DEFAULT now()
);

-- Profile Table (One-to-One with User)
CREATE TABLE profile (
  id SERIAL PRIMARY KEY,
  user_id INTEGER UNIQUE NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  created_at TIMESTAMP DEFAULT now()
);

-- Allergy Table
CREATE TABLE allergy (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT now()
);

-- Profile Allergy (Many-to-Many)
CREATE TABLE profile_allergy (
  profile_id INTEGER NOT NULL REFERENCES profile(id) ON DELETE CASCADE,
  allergy_id INTEGER NOT NULL REFERENCES allergy(id) ON DELETE CASCADE,
  created_at TIMESTAMP DEFAULT now(),
  PRIMARY KEY (profile_id, allergy_id)
);

-- Flavor Table
CREATE TABLE flavor (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT now()
);

-- Profile Flavor Preference
CREATE TABLE profile_flavor_preference (
  profile_id INTEGER NOT NULL REFERENCES profile(id) ON DELETE CASCADE,
  flavor_id INTEGER NOT NULL REFERENCES flavor(id) ON DELETE CASCADE,
  preference_level INTEGER CHECK (preference_level BETWEEN 1 AND 10),
  created_at TIMESTAMP DEFAULT now(),
  PRIMARY KEY (profile_id, flavor_id)
);

-- Dish Table (Normalized for Data Consistency)
CREATE TABLE dish (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT now()
);

-- Dish History Table
CREATE TABLE dish_history (
  id SERIAL PRIMARY KEY,
  profile_id INTEGER NOT NULL REFERENCES profile(id) ON DELETE CASCADE,
  dish_id INTEGER NOT NULL REFERENCES dish(id) ON DELETE CASCADE,
  user_rating INTEGER CHECK (user_rating BETWEEN 1 AND 5),
  created_at TIMESTAMP DEFAULT now()
);

-- Constraint Type
CREATE TABLE constraint_type (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL,
  created_at TIMESTAMP DEFAULT now()
);

-- Profile Constraints (Renamed for Clarity)
CREATE TABLE profile_constraint (
  id SERIAL PRIMARY KEY,
  profile_id INTEGER NOT NULL REFERENCES profile(id) ON DELETE CASCADE,
  constraint_type_id INTEGER NOT NULL REFERENCES constraint_type(id) ON DELETE CASCADE,
  created_at TIMESTAMP DEFAULT now()
);

-- Special Preferences Table
CREATE TABLE special_preference (
  id SERIAL PRIMARY KEY,
  profile_id INTEGER NOT NULL REFERENCES profile(id) ON DELETE CASCADE,
  description TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT now()
);

-- Indexes for Performance
CREATE INDEX idx_user_username ON app_user(username);
CREATE INDEX idx_refresh_token ON refresh_tokens (token);
CREATE INDEX idx_user_email ON app_user(email);
CREATE INDEX idx_profile_user_id ON profile(user_id);
CREATE INDEX idx_role_name ON app_role(name);
CREATE INDEX idx_flavor_name ON flavor(name);
CREATE INDEX idx_user_role_mapping_user_id ON app_user_role_mapping(user_id);
CREATE INDEX idx_user_role_mapping_role_id ON app_user_role_mapping(role_id);
CREATE INDEX idx_user_status_user_id ON app_user_status(user_id);
CREATE INDEX idx_user_status_status_id ON app_user_status(status_type_id);
CREATE INDEX idx_profile_allergy_profile_id ON profile_allergy(profile_id);
CREATE INDEX idx_profile_allergy_allergy_id ON profile_allergy(allergy_id);
CREATE INDEX idx_profile_flavor_preference_profile_id ON profile_flavor_preference(profile_id);
CREATE INDEX idx_profile_flavor_preference_flavor_id ON profile_flavor_preference(flavor_id);
CREATE INDEX idx_dish_history_profile_id ON dish_history(profile_id);
CREATE INDEX idx_dish_history_dish_id ON dish_history(dish_id);