package com.oauth2lab.labe01authcodeserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.ApprovalStoreUserApprovalHandler;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServer extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Bean
    public TokenStore tokenStore(){

      return   new JdbcTokenStore(dataSource);
    }


//    /** * 使用JDBC数据库方式来保存用户的授权批准记录 * @return */
    @Bean
    public JdbcApprovalStore approvalStore() {
        return new JdbcApprovalStore(dataSource);
    }

    /** * 使用JDBC数据库方式来保存授权码 * @return */
    @Bean
    public AuthorizationCodeServices authorizationCodeServices() {
        return new JdbcAuthorizationCodeServices(dataSource);
    }

    @Bean
    @Primary
    public DefaultTokenServices tokenService() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        //配置token存储
        tokenServices.setTokenStore(tokenStore());
        //开启支持refresh_token，此处如果之前没有配置，启动服务后再配置重启服务，可能会导致不返回token的问题，解决方式：清除redis对应token存储
        tokenServices.setSupportRefreshToken(true);
        //复用refresh_token
        tokenServices.setReuseRefreshToken(true);
        //token有效期，设置12小时
        //refresh_token有效期，设置一周
        tokenServices.setRefreshTokenValiditySeconds(7 * 24 * 60 * 60);
        return tokenServices;
    }


    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource);
//                withClient("my-client-with-registered-redirect")
//                .authorizedGrantTypes("authorization_code")
//                .scopes("read")
//                .redirectUris("http://localhost:9001/callback");
//        clients.inMemory()
//                .withClient("clientapp")
//                .secret("112233")
//                .redirectUris("http://localhost:9001/callback")
//                //授权码模式
//                .authorizedGrantTypes("authorization_code")
//                .scopes("read_userinfo","read_contacts");
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security){

        security.allowFormAuthenticationForClients().
                passwordEncoder(NoOpPasswordEncoder.getInstance())
//                //客户端校验token访问许可
                .checkTokenAccess("permitAll()")
//                //客户端token调用许可
                .tokenKeyAccess("permitAll()");


    }

//    @Bean
//    public ApprovalStoreUserApprovalHandler getApprovalStoreUserApprovalHandler(){
//       ApprovalStoreUserApprovalHandler approvalStoreUserApprovalHandler =  new ApprovalStoreUserApprovalHandler();
//       approvalStoreUserApprovalHandler.setApprovalStore(approvalStore());
//       return  approvalStoreUserApprovalHandler;
//    }


    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

        endpoints.authorizationCodeServices(authorizationCodeServices())
                .tokenStore(tokenStore())
                .tokenServices(tokenService()).approvalStore(approvalStore());
    }


}