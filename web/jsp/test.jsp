<html>
<head>
    <link href="../static/css/jquery-ui.css" rel="stylesheet">
    <link href="../static/css/jquery-ui.theme.css" rel="stylesheet">
    <link href="../static/css/font-awesome.css" rel="stylesheet">
    <link href="../static/css/fira.css" rel="stylesheet">
    <link rel="stylesheet" href="../static/chessground/base.css">
    <link rel="stylesheet" href="../static/chessground/theme.css">
    <link rel="stylesheet" href="../static/chessground/desktop.css">

    <title></title>
    <style>
        .versus_ai_toggle {
            display: block;
            border: black 1px solid;
            color: #f5f5f5;
            font-weight: bold;
            font-family: FiraSans;
            margin: 2px;
            width: 110px;
        }
    </style>
</head>


<body>
    <div id="container">

    </div>

</body>


<script src="../static/js/libs/jquery-1.10.1.min.js"></script>
<script src="../static/js/libs/jquery-ui.js"></script>
<script src="../static/chessground/chessground.min.js"></script>
<script src="../static/js/libs/chess.js"></script>
<script src="../static/js/libs/Chart.min.js"></script>
<script>
    var vsAI = $("<a>",{'href': "#" , 'class' :"versus_ai_toggle"})
            .append($("<span>").text("vs Machine "))
            .append($("<i>",{'class' : "fa fa-desktop"}));
    $("#container").append(vsAI);
    var selected = false;
    var UNSELECTED_COLOR = "#d67874";
    var SELECTED_COLOR = "rgb(161, 219, 22)";
    vsAI.css("background", UNSELECTED_COLOR).click(function () {
        selected = !selected;
        $(this).css("background", selected ? SELECTED_COLOR : UNSELECTED_COLOR);
    });

</script>
</html>
