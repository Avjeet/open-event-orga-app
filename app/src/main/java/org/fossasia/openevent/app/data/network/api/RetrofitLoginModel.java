package org.fossasia.openevent.app.data.network.api;

import org.fossasia.openevent.app.contract.model.LoginModel;
import org.fossasia.openevent.app.contract.model.UtilModel;
import org.fossasia.openevent.app.data.models.Login;
import org.fossasia.openevent.app.data.models.LoginResponse;
import org.fossasia.openevent.app.utils.Constants;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RetrofitLoginModel implements LoginModel {

    private UtilModel utilModel;
    private String token;
    private LoginService loginService;

    public RetrofitLoginModel(UtilModel utilModel) {
        this.utilModel = utilModel;
    }

    @Override
    public boolean isLoggedIn() {
        if(token == null)
            token = utilModel.getString(Constants.SHARED_PREFS_TOKEN, null);
        return token != null;
    }

    @Override
    public Observable<LoginResponse> login(String username, String password) {

        if(isLoggedIn()) {
            return Observable.just(new LoginResponse(token));
        }

        if(!utilModel.isConnected()) {
            return Observable.error(new RuntimeException(Constants.NO_NETWORK));
        }

        if(loginService == null)
            loginService = NetworkService.getLoginService();

        return loginService
            .login(new Login(username, password))
            .doOnNext(loginResponse -> saveToken(loginResponse.getAccessToken()))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    private void saveToken(String token) {
        utilModel.saveString(Constants.SHARED_PREFS_TOKEN, token);
    }

    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
    }
}