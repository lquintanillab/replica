/*
grails {
    // Mailjet
    mail{
        port= 465
        host= "in-v3.mailjet.com"
        username= "1d98821252f0e19ba4d9a6f678a8f129"
        password='${mail.password}'
        props= ["mail.smtp.auth":"true",
                "mail.smtp.socketFactory.port":"465",
                "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
                "mail.smtp.socketFactory.fallback":"false"
                //"mail.smtp.ssl.trust":"smtp.mailgun.com"
        ]
    }
}
*/
grails {
    // Gmail
    mail{
        port= 465
        host= 'smtp.gmail.com'
        username='bdintegracion@gmail.com'
        password='siipaprx_2108'
        props= ["mail.smtp.auth":"true",
                "mail.smtp.socketFactory.port":"465",
                "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
                "mail.smtp.socketFactory.fallback":"false"
        ]
    }
}