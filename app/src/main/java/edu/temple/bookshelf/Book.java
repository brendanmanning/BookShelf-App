package edu.temple.bookshelf;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Book implements Parcelable {

    private int id;
    private String title;
    private String author;
    private String coverUrl;
    private int duration;

    public Book(int id, String title, String author, String coverUrl, int duration) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
        this.duration = duration;
    }

    protected Book(Parcel in) {
        id = in.readInt();
        title = in.readString();
        author = in.readString();
        coverUrl = in.readString();
        duration = in.readInt();
    }

    public static Book fromJson(JSONObject json) throws JSONException {
        return new Book(
            json.getInt("id"),
            json.getString("title"),
            json.getString("author"),
            json.getString("coverUrl"),
            json.getInt("duration")
        );
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", this.getId());
        jsonObject.put("title", this.getTitle());
        jsonObject.put("author", this.getAuthor());
        jsonObject.put("coverUrl", this.getCoverUrl());
        jsonObject.put("duration", this.getDuration());
        return jsonObject;
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(author);
        parcel.writeString(coverUrl);
        parcel.writeInt(duration);
    }
}
