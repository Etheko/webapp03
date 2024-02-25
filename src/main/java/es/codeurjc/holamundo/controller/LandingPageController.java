package es.codeurjc.holamundo.controller;

import es.codeurjc.holamundo.entity.Author;
import es.codeurjc.holamundo.entity.Book;
import es.codeurjc.holamundo.entity.Genre;
import es.codeurjc.holamundo.repository.AuthorRepository;
import es.codeurjc.holamundo.repository.BookRepository;
import es.codeurjc.holamundo.repository.GenreRepository;
import es.codeurjc.holamundo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;


@Controller
public class LandingPageController {

    private String testingCurrentUsername;

    private boolean isUser;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorRepository authorRepository;


    //Method that will load the landing page
    @GetMapping("/")
    public String loadLandingPage(Model model, HttpServletRequest request) throws SQLException {

        Authentication authentication = (Authentication) request.getUserPrincipal();
        if (authentication != null) {
            testingCurrentUsername = authentication.getName();
            isUser = true;
        } else {
            isUser = false;
        }


        model.addAttribute("user", isUser);
        model.addAttribute("username", testingCurrentUsername);

        // Recommendations algorithm---------------------------------------------------------------
        // If it is a registered user, get the most read genres
        // Get current user most read genres
        List<Genre> mostReadGenres;

        //Get current user most read authors
        List<Author> mostReadAuthors;

        // Get books from the most read genres (these will be the recommended books)
        List<Book> booksFromMostReadGenres;

        // Get books from the most read author (the first one on the list)
        List<Book> booksFromMostReadAuthor;

        // Load lists if a user is logged in
        if (isUser) {
            mostReadGenres = userRepository.getMostReadGenres(testingCurrentUsername);
            mostReadAuthors = userRepository.getMostReadAuthors(testingCurrentUsername);
            model.addAttribute("profilePicture", userRepository.getProfileImageByUsername(testingCurrentUsername));

            // check if the user has read any books
            if (mostReadGenres.size() == 0) {
                // If the user has not read any books, get the most read genres from the database
                mostReadGenres = genreRepository.getMostReadGenres();
            }
            if (mostReadAuthors.size() == 0) {
                // If the user has not read any books, get the most read authors from the database
                mostReadAuthors = authorRepository.getMostReadAuthors();
            }

        } else {
            // If it is not a registered user, get the most read genres from the database
            mostReadGenres = genreRepository.getMostReadGenres();
            mostReadAuthors = authorRepository.getMostReadAuthors();
        }
        booksFromMostReadGenres = bookRepository.findByGenreIn(mostReadGenres, PageRequest.of(0, 4)).getContent();
        booksFromMostReadAuthor = bookRepository.findByAuthorString(mostReadAuthors.get(0).getName(), PageRequest.of(0, 5)).getContent();
        //Get info of the book recommended in the big card (recommended by author)

        long bookID = booksFromMostReadAuthor.get(0).getID();
        String bookTitle = booksFromMostReadAuthor.get(0).getTitle();
        String bookAuthor = booksFromMostReadAuthor.get(0).getAuthorString();
        String bookDescription = booksFromMostReadAuthor.get(0).getDescription();
        //String bookImage = booksFromMostReadAuthor.get(0).getImage();

        //Convert the imageFile to Base64 for it to appear in html
        Blob blob = booksFromMostReadAuthor.get(0).getImageFile();
        byte[] bytes = blob.getBytes(1, (int) blob.length());
        String bookImage = Base64.getEncoder().encodeToString(bytes);
        
        model.addAttribute("mostReadAuthor", mostReadAuthors.get(0).getName());

        model.addAttribute("bookTitle", bookTitle);
        model.addAttribute("bookAuthor", bookAuthor);
        model.addAttribute("bookDescription", bookDescription);
        model.addAttribute("bookImage", bookImage);
        model.addAttribute("bookID", bookID);
        model.addAttribute("bookDate", booksFromMostReadGenres.get(0).getReleaseDate());
        model.addAttribute("genre", booksFromMostReadGenres.get(0).getGenre().getName());


        // Split the list of recommended books into two lists to display them in two columns
        List<Book> recommendedBooksLeft;
        List<Book> recommendedBooksRight;

        recommendedBooksLeft = booksFromMostReadGenres.subList(0, (booksFromMostReadGenres.size() / 2));
        recommendedBooksRight = booksFromMostReadGenres.subList((booksFromMostReadGenres.size() / 2), booksFromMostReadGenres.size());

        for(int i=0;i<recommendedBooksRight.size();i++){
            recommendedBooksRight.get(i).setImageString(recommendedBooksRight.get(i).blobToString(recommendedBooksRight.get(i).getImageFile()));
        }

        for(int i=0;i<recommendedBooksLeft.size();i++){
            recommendedBooksLeft.get(i).setImageString(recommendedBooksLeft.get(i).blobToString(recommendedBooksLeft.get(i).getImageFile()));
        }

        model.addAttribute("postL", recommendedBooksLeft);
        model.addAttribute("postR", recommendedBooksRight);

        //Admin
        model.addAttribute("admin", request.isUserInRole("ADMIN"));

        return "landingPage";
    }

    //Method that will load 4 more posts
    @GetMapping("/landingPage/loadMore")
    public String loadLandingPagePosts(Model model, @RequestParam int page, @RequestParam int size) throws SQLException {
        // If it is a registered user, get the most read genres
        // Get current user most read genres
        List<Genre> mostReadGenres = userRepository.getMostReadGenres(testingCurrentUsername);

        // Get books from the most read genres (these will be the recommended books)
        List<Book> booksFromMostReadGenres = bookRepository.findByGenreIn(mostReadGenres, PageRequest.of(page, size)).getContent();

        for(int i=0;i<booksFromMostReadGenres.size();i++){
            booksFromMostReadGenres.get(i).setImageString(booksFromMostReadGenres.get(i).blobToString(booksFromMostReadGenres.get(i).getImageFile()));
        }
        
        model.addAttribute("post", booksFromMostReadGenres);

        return "landingPagePostTemplate";

    }

    @GetMapping("/landingPage/mostReadGenres")
    public ResponseEntity<List<Genre>> getMostReadGenres() {
        List<Genre> mostReadGenres = genreRepository.getMostReadGenres();
        System.out.println(mostReadGenres);
        return new ResponseEntity<>(mostReadGenres, HttpStatus.OK);
    }
}

