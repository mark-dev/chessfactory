<html>
<head>
    <title>Login Page</title>
    <link rel="icon" href="../static/img/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="../static/img/favicon.ico" type="image/xicon">
    <link rel="stylesheet" href="../static/css/authpage.css">
</head>

<body onload='document.f.username.focus();'>


<div class="header_div">
    <h3 class="header_text">Welcome to Chess Factory! </h3>
</div>
<form name='f' action='/j_spring_security_check' method='POST'>
    <div>
        <span class="workers_div">
            <img style="width:100%" src="../static/img/maintenance.jpg">
        </span>

        <div class="auth_block">

            <h3 class="namequery">What is your name?</h3>
                <span>
                    <input type='text' name='username' value=''>
                    <input type='hidden' name='password'/>
                     <input name="submit" type="submit" value=" "/>
                    </span>

        </div>
    </div>
</form>
</body>
</html>
