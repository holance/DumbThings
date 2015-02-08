package org.lunci.dumbthing.dataModel;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Observable;

/**
 * Created by Lunci on 2/1/2015.
 */
public class DumbModel extends Observable implements Parcelable {
    public static final String Table = "DumbThings";
    public static final String Id_Field = "id";
    public static final String CreatedAt_Field = "CreatedAt";
    public static final String ModifiedAt_Field = "ModifiedAt";
    public static final String Content_Field = "Content";
    public static final String MediaId_Field = "MediaId";
    public static final String MediaContent_Field = "MediaContent";
    public static final Parcelable.Creator<DumbModel> CREATOR = new Parcelable.Creator<DumbModel>() {
        public DumbModel createFromParcel(Parcel source) {
            return new DumbModel(source);
        }

        public DumbModel[] newArray(int size) {
            return new DumbModel[size];
        }
    };
    private long mId=-1;
    private String mCreatedAt="";
    private String mModifiedAt="";
    private String mContent="";
    private Uri mMediaContent=Uri.EMPTY;
    private long mMediaId=-1;

    public DumbModel() {

    }

    public DumbModel(long id) {
        this();
        mId = id;
    }

    public DumbModel(long id, String createdAt) {
        this(id);
        mCreatedAt = createdAt;
    }

    public DumbModel(long id, String createdAt, String content) {
        this(id, createdAt);
        mContent = content;
    }

    public DumbModel(long id, String createdAt, String content, Uri mediaContent) {
        this(id, createdAt, content);
        mMediaContent = mediaContent;
    }

    private DumbModel(Parcel in) {
        this.mId = in.readLong();
        this.mCreatedAt = in.readString();
        this.mModifiedAt = in.readString();
        this.mContent = in.readString();
        this.mMediaContent = in.readParcelable(Uri.class.getClassLoader());
        this.mMediaId = in.readLong();
    }

    public String getModifiedAt() {
        return mModifiedAt;
    }

    public void setModifiedAt(String modifiedAt) {
        if(mModifiedAt.equals(modifiedAt))
            return;
        this.mModifiedAt = modifiedAt;
        setChanged();
        notifyObservers(ModifiedAt_Field);
    }

    public long getMediaId() {
        return mMediaId;
    }

    public void setMediaId(long mediaId) {
        if(mMediaId==mediaId)return;
        this.mMediaId = mediaId;
        setChanged();
        notifyObservers(MediaId_Field);
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        if(mId==id)return;
        this.mId = id;
        setChanged();
        notifyObservers(Id_Field);
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        if(mCreatedAt.equals(createdAt))return;
        this.mCreatedAt = createdAt;
        setChanged();
        notifyObservers(CreatedAt_Field);
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        if(mContent.equals(content))return;
        this.mContent = content;
        setChanged();
        notifyObservers(Content_Field);
    }

    public Uri getMediaContent() {
        return mMediaContent;
    }

    public void setMediaContent(Uri mediaContent) {
        if(mMediaContent.equals(mediaContent))
            return;
        this.mMediaContent = mediaContent;
        setChanged();
        notifyObservers(MediaContent_Field);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mId);
        dest.writeString(this.mCreatedAt);
        dest.writeString(this.mModifiedAt);
        dest.writeString(this.mContent);
        dest.writeParcelable(this.mMediaContent, 0);
        dest.writeLong(this.mMediaId);
    }
}
