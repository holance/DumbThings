package org.lunci.dumbthing.dataModel;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lunci on 2/1/2015.
 */
public class DumbModel implements Parcelable {
    public static final String Table = "DumbThings";
    public static final String Id_Field = "id";
    public static final String CreatedAt_Field = "CreatedAt";
    public static final String ModifiedAt_Field = "ModifiedAt";
    public static final String Content_Field = "Content";
    public static final String MediaId_Field = "MediaId";
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
        this.mModifiedAt = modifiedAt;
    }

    public long getMediaId() {
        return mMediaId;
    }

    public void setMediaId(long mMediaId) {
        this.mMediaId = mMediaId;
    }

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String mCreatedAt) {
        this.mCreatedAt = mCreatedAt;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }

    public Uri getMediaContent() {
        return mMediaContent;
    }

    public void setMediaContent(Uri mediaContent) {
        this.mMediaContent = mediaContent;
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
