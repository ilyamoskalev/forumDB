package application.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

    private String about;
    private String email;
    private String fullname;
    private String nickname;

    @JsonCreator
    public User(@JsonProperty("about") String about,
                @JsonProperty("email") String email,
                @JsonProperty("fullname") String fullname) {

        this.email = email;
        this.fullname = fullname;
        this.about = about;
    }

    public User(String about, String email, String fullname, String nickname) {
        this.email = email;
        this.fullname = fullname;
        this.about = about;
        this.nickname = nickname;
    }

    public String getAbout() {
        return about;
    }

    public String getEmail() {
        return email;
    }

    public String getFullname() {
        return fullname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


}
