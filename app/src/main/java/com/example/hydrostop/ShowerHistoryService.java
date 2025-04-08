package com.example.hydrostop;

import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;

public interface ShowerHistoryService {
    @GET("showerhistory/")
    Call<List<ShowerHistory>> getShowerHistory();
}


