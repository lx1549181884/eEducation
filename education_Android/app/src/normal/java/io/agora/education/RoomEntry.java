package io.agora.education;

import android.os.Parcel;
import android.os.Parcelable;

import io.agora.education.lx.UserProperty;

/**
 * @author cjw
 */
public class RoomEntry implements Parcelable {
    private String userName;
    private String userUuid;
    private String roomName;
    private String roomUuid;
    private int roomType;
    private @UserProperty.jhbRole
    int role;

    public RoomEntry(String userName, String userUuid, String roomName, String roomUuid, int roomType, @UserProperty.jhbRole int role) {
        this.userName = userName;
        this.userUuid = userUuid;
        this.roomName = roomName;
        this.roomUuid = roomUuid;
        this.roomType = roomType;
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomUuid() {
        return roomUuid;
    }

    public void setRoomUuid(String roomUuid) {
        this.roomUuid = roomUuid;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public @UserProperty.jhbRole
    int getRole() {
        return role;
    }

    public void setRole(@UserProperty.jhbRole int role) {
        this.role = role;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userName);
        dest.writeString(this.userUuid);
        dest.writeString(this.roomName);
        dest.writeString(this.roomUuid);
        dest.writeInt(this.roomType);
        dest.writeInt(this.role);
    }

    protected RoomEntry(Parcel in) {
        this.userName = in.readString();
        this.userUuid = in.readString();
        this.roomName = in.readString();
        this.roomUuid = in.readString();
        this.roomType = in.readInt();
        this.role = in.readInt();
    }

    public static final Creator<RoomEntry> CREATOR = new Creator<RoomEntry>() {
        @Override
        public RoomEntry createFromParcel(Parcel source) {
            return new RoomEntry(source);
        }

        @Override
        public RoomEntry[] newArray(int size) {
            return new RoomEntry[size];
        }
    };
}
