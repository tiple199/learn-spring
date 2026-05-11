package vn.hoidanit.springrestwithai.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.hoidanit.springrestwithai.model.User;
import vn.hoidanit.springrestwithai.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public User getUserById(Long id) {
        // findById trả về Optional<User>, dùng orElse(null) để giữ nguyên kiểu trả về User
        return this.userRepository.findById(id).orElse(null);
    }

    public User createUser(User user) {
        return this.userRepository.save(user);
    }

    public User updateUser(User user) {
        // JPA save() tự động UPDATE nếu entity đã có id tồn tại trong database
        return this.userRepository.save(user);
    }

    public void deleteUser(Long id) {
        this.userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }
}
