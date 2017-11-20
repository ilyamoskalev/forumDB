package application.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Vote {
    private Integer id;
    private String nickname;
    private int voice;
    private Integer thread;

    public Vote(@JsonProperty("nickname") String nickname,
                @JsonProperty("vote") int voice) {

        this.nickname = nickname;
        this.voice = voice;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getVoice() {
        return voice;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }
}
