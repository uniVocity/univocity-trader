import com.univocity.trader.exchange.tdameritrade.api.auth.TDAAuthenticator;
import org.junit.Test;

public class TestAuth {


    @Test
    public void testAuth(){
        String authURL = "";
        String driverPath = "";
        TDAAuthenticator authenticator = new TDAAuthenticator(authURL, driverPath);
        authenticator.authenticate();
    }
}
