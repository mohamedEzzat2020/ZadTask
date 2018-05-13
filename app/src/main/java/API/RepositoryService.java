package API;

import java.util.List;

import DataModels.Repository;
import DataModels.RepositoryResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface RepositoryService {

    @GET("square/repos")
    Call<List<Repository>> getRepositories(
            @Query("page") int page,
            @Query("per_page") int per_page
    );

}
