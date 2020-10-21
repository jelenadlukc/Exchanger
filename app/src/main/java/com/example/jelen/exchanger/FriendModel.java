package com.example.jelen.exchanger;


public class FriendModel {

        private String name;
      //  private int points;
        private String uId;

        public FriendModel(String name, String uId)
        {
            this.name = name;
           // this.points = points;
            this.uId = uId;
        }

        String getName()
        {
            return name;
        }

        void setName(String name)
        {
            this.name = name;
        }

      //  public int getPoints()
      //  {
      //      return points;
      //  }

      //  public void setPoints(int points)
      //  {
       //     this.points = points;
      //  }

        public String getuId()
        {
            return uId;
        }

        void setuId(String uId)
        {
            this.uId = uId;
        }
    }
