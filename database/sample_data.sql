-- Sample data for DevSync application
-- Run this after creating the main schema

-- Insert sample users (passwords are 'password123' hashed with BCrypt)
INSERT INTO users (username, email, password, first_name, last_name, status, is_online) VALUES 
    ('admin', 'admin@devsync.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin', 'User', 'ACTIVE', TRUE),
    ('caleb_adams', 'caleb@devsync.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Caleb', 'Adams', 'ACTIVE', TRUE),
    ('michael_oti', 'michael@devsync.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Michael', 'Oti Yamoah', 'AWAY', FALSE),
    ('hakeem_adam', 'hakeem@devsync.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Hakeem', 'Adam', 'ACTIVE', TRUE),
    ('frank_iokko', 'frank@devsync.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Frank', 'Iokko', 'DO_NOT_DISTURB', FALSE),
    ('alvin', 'alvin@devsync.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Alvin', '', 'ACTIVE', TRUE),
    ('selormfidel', 'selorm@devsync.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Selorm', 'Fidel', 'OFFLINE', FALSE),
    ('roger_osafo', 'roger@devsync.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Roger', 'Osafo Kwabena Adu', 'ACTIVE', FALSE)
ON CONFLICT (username) DO NOTHING;

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES 
    (1, 3), -- admin gets ROLE_ADMIN
    (1, 2), -- admin gets ROLE_MODERATOR
    (1, 1), -- admin gets ROLE_USER
    (2, 1), -- caleb_adams gets ROLE_USER
    (3, 1), -- michael_oti gets ROLE_USER
    (4, 1), -- hakeem_adam gets ROLE_USER
    (5, 2), -- frank_iokko gets ROLE_MODERATOR
    (5, 1), -- frank_iokko gets ROLE_USER
    (6, 1), -- alvin gets ROLE_USER
    (7, 1), -- selormfidel gets ROLE_USER
    (8, 1)  -- roger_osafo gets ROLE_USER
ON CONFLICT DO NOTHING;

-- Insert default channels
INSERT INTO channels (name, description, is_private, created_by) VALUES 
    ('general', 'General discussions for everyone', FALSE, 1),
    ('random', 'Random conversations and fun stuff', FALSE, 1),
    ('announcements', 'Important announcements and updates', FALSE, 1)
ON CONFLICT DO NOTHING;

-- Update the default channels to use the admin user
UPDATE channels SET created_by = 1 WHERE created_by = 1;

-- Insert additional channels
INSERT INTO channels (name, description, is_private, created_by) VALUES 
    ('backend', 'Backend development discussions', FALSE, 1),
    ('frontend', 'Frontend development discussions', FALSE, 1),
    ('design', 'UI/UX design discussions', FALSE, 1),
    ('testing', 'Testing and QA discussions', FALSE, 1),
    ('devops', 'DevOps and deployment discussions', TRUE, 1),
    ('team-leads', 'Private channel for team leads', TRUE, 1)
ON CONFLICT DO NOTHING;

-- Add users to channels
INSERT INTO channel_members (channel_id, user_id) VALUES 
    -- Everyone in general
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
    -- Everyone in random
    (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8),
    -- Everyone in announcements
    (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8),
    -- Backend team
    (4, 1), (4, 3), (4, 6), (4, 8),
    -- Frontend team
    (5, 1), (5, 2), (5, 4), (5, 7),
    -- Design team
    (6, 1), (6, 2), (6, 5),
    -- Testing team
    (7, 1), (7, 4), (7, 5),
    -- DevOps (private)
    (8, 1), (8, 8),
    -- Team leads (private)
    (9, 1), (9, 5)
ON CONFLICT DO NOTHING;

-- Insert sample messages
INSERT INTO messages (content, sender_id, channel_id, type) VALUES 
    ('Welcome to DevSync! 🎉', 1, 1, 'TEXT'),
    ('Thanks for setting this up!', 2, 1, 'TEXT'),
    ('Looking forward to collaborating here', 3, 1, 'TEXT'),
    ('Great to have everyone on board', 4, 1, 'TEXT'),
    ('Let''s build something amazing together! 🚀', 5, 1, 'TEXT'),
    
    -- Random channel messages
    ('Anyone up for a coffee break? ☕', 2, 2, 'TEXT'),
    ('Just finished the new feature implementation', 3, 2, 'TEXT'),
    ('The weather is great today! 🌞', 6, 2, 'TEXT'),
    
    -- Backend channel messages
    ('Working on the authentication system', 3, 4, 'TEXT'),
    ('Database schema is ready for review', 6, 4, 'TEXT'),
    ('API endpoints are documented', 8, 4, 'TEXT'),
    
    -- Frontend channel messages
    ('New UI components are ready', 2, 5, 'TEXT'),
    ('Mobile responsiveness looks good', 4, 5, 'TEXT'),
    ('Testing the new chat interface', 7, 5, 'TEXT'),
    
    -- Announcements
    ('Sprint planning meeting tomorrow at 10 AM', 1, 3, 'TEXT'),
    ('Please update your profiles with your contact info', 1, 3, 'TEXT')
ON CONFLICT DO NOTHING;

-- Insert sample direct messages
INSERT INTO messages (content, sender_id, recipient_id, type) VALUES 
    ('Hey, can you review the PR when you get a chance?', 2, 3, 'TEXT'),
    ('Sure! I''ll take a look this afternoon', 3, 2, 'TEXT'),
    ('Thanks! No rush though', 2, 3, 'TEXT'),
    
    ('Great work on the backend API!', 4, 3, 'TEXT'),
    ('Thanks! The authentication system is working well now', 3, 4, 'TEXT'),
    
    ('Can you help me with the frontend styling?', 2, 4, 'TEXT'),
    ('Of course! What specific issue are you facing?', 4, 2, 'TEXT'),
    
    ('Meeting reminder: Code review at 2 PM', 1, 5, 'TEXT'),
    ('Got it! I''ll be there', 5, 1, 'TEXT'),
    
    ('The new feature is deployed to staging', 8, 1, 'TEXT'),
    ('Perfect! Let''s test it thoroughly', 1, 8, 'TEXT')
ON CONFLICT DO NOTHING; 