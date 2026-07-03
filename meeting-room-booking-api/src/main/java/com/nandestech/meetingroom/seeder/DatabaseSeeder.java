package com.nandestech.meetingroom.seeder;

import com.nandestech.meetingroom.entity.Department;
import com.nandestech.meetingroom.entity.Room;
import com.nandestech.meetingroom.entity.User;
import com.nandestech.meetingroom.repository.DepartmentRepository;
import com.nandestech.meetingroom.repository.RoomRepository;
import com.nandestech.meetingroom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("seed")
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoomRepository roomRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        try {
            seedSuperAdmin();
            seedSampleUsers();
            seedSampleRooms();
            System.out.println("=== Database seeding completed successfully ===");
        } catch (RuntimeException e) {
            System.out.println("Seeding error: " + e.getMessage());
        }
    }

    private void seedSuperAdmin() {
        if (userRepository.findByUsername("superadmin").isPresent()) {
            System.out.println("Super Admin already exists, skipping");
            return;
        }

        // Assign to first department (VTT)
        Department vtt = departmentRepository.findByCode("VTT").orElse(null);

        User superAdmin = new User();
        superAdmin.setName("Super Admin");
        superAdmin.setUsername("superadmin");
        superAdmin.setEmail("admin@viettel.com.vn");
        superAdmin.setPassword(passwordEncoder.encode("password"));
        superAdmin.setRole("ADMIN");
        superAdmin.setDepartmentId(vtt != null ? vtt.getId() : null);
        superAdmin.setCreatedAt(LocalDateTime.now());
        superAdmin.setUpdatedAt(LocalDateTime.now());

        userRepository.save(superAdmin);
        System.out.println("✓ Super Admin seeded (username: superadmin, password: password)");
    }

    private void seedSampleUsers() {
        List<Department> departments = departmentRepository.findAll();
        if (departments.isEmpty()) {
            System.out.println("No departments found, skipping user seeding");
            return;
        }

        // Create an APPROVER for each department
        for (Department dept : departments) {
            String username = "approver." + dept.getCode().toLowerCase();
            if (userRepository.findByUsername(username).isPresent()) continue;

            User approver = new User();
            approver.setName("Approver " + dept.getCode());
            approver.setUsername(username);
            approver.setEmail("approver." + dept.getCode().toLowerCase() + "@viettel.com.vn");
            approver.setPassword(passwordEncoder.encode("password"));
            approver.setRole("APPROVER");
            approver.setDepartmentId(dept.getId());
            approver.setCreatedAt(LocalDateTime.now());
            approver.setUpdatedAt(LocalDateTime.now());
            userRepository.save(approver);
        }
        System.out.println("✓ Approver accounts seeded (one per department)");

        // Create a sample EMPLOYEE for VHT and VCS
        seedEmployee("nguyen.van.a", "Nguyen Van A", "nguyen.van.a@viettel.com.vn", "VHT");
        seedEmployee("tran.thi.b", "Tran Thi B", "tran.thi.b@viettel.com.vn", "VCS");
        seedEmployee("le.van.c", "Le Van C", "le.van.c@viettel.com.vn", "VTIT");
        System.out.println("✓ Sample employee accounts seeded");
    }

    private void seedEmployee(String username, String name, String email, String deptCode) {
        if (userRepository.findByUsername(username).isPresent()) return;

        Department dept = departmentRepository.findByCode(deptCode).orElse(null);
        User employee = new User();
        employee.setName(name);
        employee.setUsername(username);
        employee.setEmail(email);
        employee.setPassword(passwordEncoder.encode("password"));
        employee.setRole("EMPLOYEE");
        employee.setDepartmentId(dept != null ? dept.getId() : null);
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        userRepository.save(employee);
    }

    private void seedSampleRooms() {
        if (roomRepository.count() > 0) {
            System.out.println("Rooms already exist, skipping room seeding");
            return;
        }

        Department vht = departmentRepository.findByCode("VHT").orElse(null);
        Department vcs = departmentRepository.findByCode("VCS").orElse(null);
        Department vtit = departmentRepository.findByCode("VTIT").orElse(null);

        createRoom("Phòng họp Everest", 20, "Tầng 10, Tòa A", "Projector,Whiteboard,Video Conference,Speaker", vht);
        createRoom("Phòng họp K2", 10, "Tầng 5, Tòa A", "Projector,Whiteboard", vht);
        createRoom("Phòng họp Sáng tạo", 8, "Tầng 3, Tòa B", "TV Screen,Whiteboard", vcs);
        createRoom("Phòng họp Bảo mật", 6, "Tầng 7, Tòa B", "Projector,Sound Proofing", vcs);
        createRoom("Phòng họp Agile", 12, "Tầng 2, Tòa C", "Projector,Whiteboard,Video Conference", vtit);
        createRoom("Phòng họp Lớn", 50, "Tầng 1, Hội trường", "Projector,Microphone,Speaker,Video Conference", null); // Shared room

        System.out.println("✓ Sample rooms seeded (6 rooms)");
    }

    private void createRoom(String name, int capacity, String location, String equipment, Department dept) {
        Room room = Room.builder()
                .name(name)
                .capacity(capacity)
                .location(location)
                .equipment(equipment)
                .isAvailable(true)
                .departmentId(dept != null ? dept.getId() : null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        roomRepository.save(room);
    }
}
