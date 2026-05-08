package vn.hoidanit.springrestwithai.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import vn.hoidanit.springrestwithai.model.User;

@Repository
public class UserRepository {

    private List<User> users = new ArrayList<>();
    private Long nextId = 4L;

    // Khởi tạo sẵn 3 user mẫu
    public UserRepository() {
        users.add(new User(1L, "Hỏi Dân IT", "hoidanit@gmail.com"));
        users.add(new User(2L, "Nguyễn Văn A", "nguyenvana@gmail.com"));
        users.add(new User(3L, "Trần Thị B", "tranthib@gmail.com"));
    }

    public List<User> findAll() {
        return this.users;
    }

    public User findById(Long id) {
        for (User user : this.users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    public User save(User user) {
        user.setId(this.nextId++);
        this.users.add(user);
        return user;
    }

    public User update(User user) {
        for (int i = 0; i < this.users.size(); i++) {
            if (this.users.get(i).getId().equals(user.getId())) {
                this.users.set(i, user);
                return user;
            }
        }
        return null;
    }

    public void deleteById(Long id) {
        this.users.removeIf(user -> user.getId().equals(id));
    }
}
