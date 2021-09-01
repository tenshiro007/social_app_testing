package com.example.socialtestingapp.model;

public class ModelComment {
    String cid,comment,timestamp,uid,uEmail,uDp,uName;

    public ModelComment() {
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public ModelComment(String cId, String comment, String timestamp, String uid, String uEmail, String uDp, String uName) {
        this.cid = cId;
        this.comment = comment;
        this.timestamp = timestamp;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uName = uName;
    }

    @Override
    public String toString() {
        return "ModelComment{" +
                "cId='" + cid + '\'' +
                ", comment='" + comment + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", uid='" + uid + '\'' +
                ", uEmail='" + uEmail + '\'' +
                ", uDp='" + uDp + '\'' +
                ", uName='" + uName + '\'' +
                '}';
    }
}
