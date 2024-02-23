package es.codeurjc.holamundo.controller;

import es.codeurjc.holamundo.entity.User;
import es.codeurjc.holamundo.repository.UserRepository;
import es.codeurjc.holamundo.service.UserList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EditProfileController {
    private UserList users;

    @Autowired
    UserRepository userRepository;

    public EditProfileController() {
        this.users = new UserList();
    }

    @GetMapping("/profile/{username}/edit")
    public String loadEditProfilePage(Model model, @PathVariable String username) {

        String alias = users.getUserInfo(username)[2];
        String role = users.getUserInfo(username)[1];
        String description = users.getUserInfo(username)[3];
        String profileImage = users.getUserInfo(username)[4];
        String email = users.getUserInfo(username)[5];
        String password = users.getUserInfo(username)[6];

        model.addAttribute("username", username);
        model.addAttribute("alias", alias);
        model.addAttribute("role", role);
        model.addAttribute("description", description);
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("email", email);
        model.addAttribute("password", password);

        return "editProfilePage";
    }

    @PostMapping("/profile/{username}/edit")
    public String editProfile(Model model, @PathVariable String username,
                              @RequestParam("alias") String newAlias,
                              @RequestParam("email") String newEmail,
                              @RequestParam("description") String newDescription,
                              @RequestParam("password") String newPassword)
    {

        User user = userRepository.findByUsername(username);

        if (user != null) {
            user.setAlias(newAlias);
            user.setEmail(newEmail);
            user.setDescription(newDescription);
            user.setPassword(newPassword);
            userRepository.save(user);
        }

        return "redirect:https://localhost:8443/profile/" + username;
    }

    @PostMapping("/profile/{username}/editPassword")
    public ResponseEntity<?> editProfile(@RequestParam("currentPassword") String currentPassword, @RequestParam("newPassword") String newPassword) {
        // Obtén el usuario actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        // Compara la contraseña actual con la almacenada en la base de datos
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(currentPassword, userDetails.getPassword())) {
            return new ResponseEntity<>("La contraseña actual es incorrecta", HttpStatus.BAD_REQUEST);
        }

        // Encripta la nueva contraseña
        String encodedNewPassword = encoder.encode(newPassword);

        // Aquí debes implementar la lógica para actualizar la contraseña del usuario en la base de datos

        return new ResponseEntity<>("Contraseña actualizada con éxito", HttpStatus.OK);
    }
}
