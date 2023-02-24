package org.example;

public class Body {
    private String swiper;
    private String swipee;


    public String getSwiper() {
        return swiper;
    }

    public void setSwiper(String swiper) {
        this.swiper = swiper;
    }

    public String getSwipee() {
        return swipee;
    }

    public boolean validInputBody(String swipee, String swiper, String comment){
        if (Integer.parseInt(swipee) < 0 || Integer.parseInt(swipee) > 1000000){
            return false;
        }
        if (Integer.parseInt(swiper) < 0 || Integer.parseInt(swiper) > 5000){
            return false;
        }
        if (comment.length() > 256){
            return false;
        }
        return true;
    }
    public void setSwipee(String swipee) {
        this.swipee = swipee;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private String comment;

    @Override
    public String toString() {
        return "Body{" +
                "swiper='" + swiper +
                ", swipee='" + swipee +
                ", comment='" + comment +
                '}';
    }
}
