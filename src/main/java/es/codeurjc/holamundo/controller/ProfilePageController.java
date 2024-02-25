package es.codeurjc.holamundo.controller;

import es.codeurjc.holamundo.entity.Book;
import es.codeurjc.holamundo.entity.User;
import es.codeurjc.holamundo.repository.BookRepository;
import es.codeurjc.holamundo.repository.UserRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;


@Controller
public class ProfilePageController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/profile/{username}/**")
    public String loadProfilePage(Model model, @PathVariable String username, HttpServletRequest request) throws SQLException {

        // Get user from the database
        User user = userRepository.findByUsername(username);

        //User info
        List<String> userRoles = user.getRole();

        // Search for admin role
        String role = "USER";
        for (String userRole : userRoles) {
            if (userRole.equals("ADMIN")) {
                role = "ADMIN";
                break;
            }
        }

        String alias = user.getAlias();
        String description = user.getDescription();
        String profileImage = user.getProfileImage();
        String email = user.getEmail();
        String password = user.getPassword();

        model.addAttribute("username", username);
        model.addAttribute("alias", alias);
        model.addAttribute("role", role);
        model.addAttribute("description", description);
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("email", email);
        model.addAttribute("password", password);

        // Get user's book lists from the database
        //UserBookLists
        int nReadBooks = user.getReadBooks().size();
        int nReadingBooks = user.getReadingBooks().size();
        int nWantedBooks = user.getWantedBooks().size();
        int nReviews = user.getReviews().size();

        List<Book> readBooksList = userRepository.getReadBooks(username, PageRequest.of(0, 4)).getContent();
        List<Book> readingBooksList = userRepository.getReadingBooks(username, PageRequest.of(0, 4)).getContent();
        List<Book> wantedBooksList = userRepository.getWantedBooks(username, PageRequest.of(0, 4)).getContent();

        for(int i=0;i<readBooksList.size();i++){
            readBooksList.get(i).setImageString(readBooksList.get(i).blobToString(readBooksList.get(i).getImageFile()));
        }

        for(int i=0;i<readingBooksList.size();i++){
            readingBooksList.get(i).setImageString(readingBooksList.get(i).blobToString(readingBooksList.get(i).getImageFile()));
        }

        for(int i=0;i<wantedBooksList.size();i++){
            wantedBooksList.get(i).setImageString(wantedBooksList.get(i).blobToString(wantedBooksList.get(i).getImageFile()));
        }
    
        List<Double> readBooksRatings = new ArrayList<>();
        readBooksList.forEach((book) -> {
            List<Double> bookRatings = bookRepository.getRatingsByBookId(book.getID());
            double averageRating = 0;
            if (bookRatings.size() > 0) {
                for (Double rating : bookRatings) {
                    averageRating += rating;
                }
                averageRating /= bookRatings.size();
            }
            readBooksRatings.add(averageRating);
        });
        model.addAttribute("ratingsRead", readBooksRatings);

        List<Double> readingBooksRatings = new ArrayList<>();
        readingBooksList.forEach((book) -> {
            List<Double> bookRatings = bookRepository.getRatingsByBookId(book.getID());
            double averageRating = 0;
            if (bookRatings.size() > 0) {
                for (Double rating : bookRatings) {
                    averageRating += rating;
                }
                averageRating /= bookRatings.size();
            }
            readingBooksRatings.add(averageRating);
        });
        model.addAttribute("ratingsReading", readingBooksRatings);

        List<Double> wantedBooksRatings = new ArrayList<>();
        wantedBooksList.forEach((book) -> {
            List<Double> bookRatings = bookRepository.getRatingsByBookId(book.getID());
            double averageRating = 0;
            if (bookRatings.size() > 0) {
                for (Double rating : bookRatings) {
                    averageRating += rating;
                }
                averageRating /= bookRatings.size();
            }
            wantedBooksRatings.add(averageRating);
        });
        model.addAttribute("ratingsWanted", wantedBooksRatings);

        model.addAttribute("nReadBooks", nReadBooks);
        model.addAttribute("nReadingBooks", nReadingBooks);
        model.addAttribute("nWantedBooks", nWantedBooks);
        model.addAttribute("nReviews", nReviews);
        model.addAttribute("ReadBooks", readBooksList);
        model.addAttribute("ReadingBooks", readingBooksList);
        model.addAttribute("WantedBooks", wantedBooksList);

        //Admin
        model.addAttribute("admin", request.isUserInRole("ADMIN"));

         //Unregistered user
         model.addAttribute("noUser", !request.isUserInRole("USER"));


        return "profilePage";
    }

    @GetMapping("/profile/{username}/loadMore")
    public String loadReadBooks(@PathVariable String username, @RequestParam(defaultValue = "default") String listType, @RequestParam int page, @RequestParam int size, Model model) throws SQLException {
        switch (listType) {
            case "read" -> {
                List<Book> readBooksList = userRepository.getReadBooks(username, PageRequest.of(page, size)).getContent();
                
                for(int i=0;i<readBooksList.size();i++){
                    readBooksList.get(i).setImageString(readBooksList.get(i).blobToString(readBooksList.get(i).getImageFile()));
                }
                
                model.addAttribute("bookItem", readBooksList);

                List<Double> readBooksRatings = new ArrayList<>();
                readBooksList.forEach((book) -> {
                    List<Double> bookRatings = bookRepository.getRatingsByBookId(book.getID());
                    double averageRating = 0;
                    if (bookRatings.size() > 0) {
                        for (Double rating : bookRatings) {
                            averageRating += rating;
                        }
                        averageRating /= bookRatings.size();
                    }
                    readBooksRatings.add(averageRating);
                });
                model.addAttribute("ratings", readBooksRatings);
            }
            case "reading" -> {
                List<Book> readingBooksList = userRepository.getReadingBooks(username, PageRequest.of(page, size)).getContent();
                
                for(int i=0;i<readingBooksList.size();i++){
                    readingBooksList.get(i).setImageString(readingBooksList.get(i).blobToString(readingBooksList.get(i).getImageFile()));
                }
                
                model.addAttribute("bookItem", readingBooksList);

                List<Double> readingBooksRatings = new ArrayList<>();
                readingBooksList.forEach((book) -> {
                    List<Double> bookRatings = bookRepository.getRatingsByBookId(book.getID());
                    double averageRating = 0;
                    if (bookRatings.size() > 0) {
                        for (Double rating : bookRatings) {
                            averageRating += rating;
                        }
                        averageRating /= bookRatings.size();
                    }
                    readingBooksRatings.add(averageRating);
                });
                model.addAttribute("ratings", readingBooksRatings);
            }
            case "wanted" -> {
                List<Book> wantedBooksList = userRepository.getWantedBooks(username, PageRequest.of(page, size)).getContent();
                
                for(int i=0;i<wantedBooksList.size();i++){
                    wantedBooksList.get(i).setImageString(wantedBooksList.get(i).blobToString(wantedBooksList.get(i).getImageFile()));
                }
                
                model.addAttribute("bookItem", wantedBooksList);

                List<Double> wantedBooksRatings = new ArrayList<>();
                wantedBooksList.forEach((book) -> {
                    List<Double> bookRatings = bookRepository.getRatingsByBookId(book.getID());
                    double averageRating = 0;
                    if (bookRatings.size() > 0) {
                        for (Double rating : bookRatings) {
                            averageRating += rating;
                        }
                        averageRating /= bookRatings.size();
                    }
                    wantedBooksRatings.add(averageRating);
                });
                model.addAttribute("ratings", wantedBooksRatings);
            }
        }

        return "bookListsItemTemplate";


    }
}