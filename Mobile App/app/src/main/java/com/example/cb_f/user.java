package com.example.cb_f;

public class user {

    public String  phoneNo,firstName,emailId,aadhar=null,id; //public made it work (declare private)

    public user(){

    }

    public user(String phoneNo, String firstName, String emailId, String aadhar, String id) {
        this.phoneNo = phoneNo;
        this.firstName = firstName;
        this.emailId = emailId;
        this.aadhar = aadhar;
        this.id = id;
    }

    public String getId() {
        return id;
    }
    public String getFirstName(){
        return firstName;
    }
    public String getEmailId(){
        return emailId;
    }
    public String getPhoneNo(){
        return phoneNo;
    }
    public String getAadhar() { return aadhar; }


    public void setId(String id) {
        this.id = id;
    }
    public void setFirstName(String firstName){
        this.firstName  = firstName;
    }
    public void setEmailId(String emailId){
        this.emailId  = emailId;
    }
    public void setPhoneNo(String phoneNo){
        this.phoneNo = phoneNo;
    }
    public void setAadhar(String address) { this.aadhar = address; }
}
