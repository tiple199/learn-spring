package vn.hoidanit.springrestwithai.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.model.User;
import vn.hoidanit.springrestwithai.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public User getUserById(Long id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", id));
    }

    public User createUser(User user) {
        if (this.userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Người dùng", "email", user.getEmail());
        }
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        return this.userRepository.save(user);
    }

    public User updateUser(Long id, User user) {
        getUserById(id); // throws ResourceNotFoundException nếu không tồn tại
        user.setId(id);
        return this.userRepository.save(user);
    }

    public void deleteUser(Long id) {
        getUserById(id); // throws ResourceNotFoundException nếu không tồn tại
        this.userRepository.deleteById(id);
    }
}
