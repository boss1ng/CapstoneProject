
const nodemailer = require('nodemailer');
const { google } = require('googleapis');

const CLIENT_ID = '93006732287-lkifv53fqi7i3j2b052sob7qi8atoebl.apps.googleusercontent.com';
const CLIENT_SECRET = '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';
const REDIRECT_URI = 'https://developers.google.com/oauthplayground';
// const REFRESH_TOKEN = '1//04Po0tQkASdTICgYIARAAGAQSNwF-L9IrW1fkpxwViNMHrgf0t0v2Kn_o8rC2MaHcA4oOAO1_IEMH7ibAtQDRrYcwP4SevVw0eHQ';
const REFRESH_TOKEN = '1//04vd3j4OxQFG2CgYIARAAGAQSNwF-L9IrzZlD5v8aEGnhuFpmvGxK3bChU0jERc7g0AijRX0mxs0Q5jLNL8wLbzfffx5oS4ZqBUI';

// const oAuth2Client = new google.auth.OAuth2(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);
// oAuth2Client.setCredentials( {refresh_token: REFRESH_TOKEN} );

const oAuth2Client = new google.auth.OAuth2(
    CLIENT_ID,
    CLIENT_SECRET,
    REDIRECT_URI
  );
oAuth2Client.setCredentials({ refresh_token: REFRESH_TOKEN });

async function sendMail() {

    try {
        const accessToken = await oAuth2Client.getAccessToken();
        
        const transport = nodemailer.createTransport({
            service: 'gmail',
            auth: {
                type: 'OAUTH2',
                user: 'capstoneproject.it.qsee@gmail.com',
                clientId: CLIENT_ID,
                clientSecret: CLIENT_SECRET,
                refreshToken: REFRESH_TOKEN,
                accessToken: accessToken
            },
        });

        const mailOptions = {
            from: 'IT CAPSTONE PROJECT (QSEE) <capstoneproject.it.qsee@gmail.com>',
            to: 'joeybryan.pintor.cics@ust.edu.ph',
            subject: 'QSee - One-Time Password',
            text: '',
            //html: 'Good day! <br><br> Your one-time password (OTP) is <b>QWER</b>. <br><br> It will expire in two (2) minutes. <br><br><br> If you have any further questions or if there is anything else we can help you with, please let us know by reaching out to us via (02) 8-298-4313. <br><br><br> Regards, <br><br><b>QSee, IT Capstone Project</b>'
            html: 'A request to reset the password for your account has been initiated. To proceed with the password reset process, please use the following one-time password (OTP): <br><br> <h2>SAMPLE OTP</h2> <br> Please enter this OTP on the password reset page <b> within the next two (2) minutes </b> to verify your identity and reset your password. <br><br> If you did not initiate this request, please disregard this email. <br><br><br> Regards, <br><br><b>QSee, IT Capstone Project</b>'
        };

        const result = await transport.sendMail(mailOptions);
        return result;
    }

    catch (error) {
        return error;
    }
}

sendMail()
    .then((result) => console.log('Email sent.', result))
    .catch((error) => console.log(error.message));