/**
 * Created by mark on 05.02.15.
 */



//gameInfo -> #{clock,white,black}
//toolTipOpen = toolTipClose = function(div);
//onclick = function()
function gu_addNewGame(gameId, game_info, onclick,toolTipOpen,toolTipClose) {
    var elem = $("<li>").attr('id', 'game-' + gameId);
    var topDiv = $("<div>");
    var gameHeader = $("<a>",{href: "#", title: ""}).addClass("game_name").on('click', onclick).html("game#" + gameId);


    var tooltipDiv;
    gameHeader.tooltip({content: function() {
        tooltipDiv = $("<div>").addClass("gamePreview fa fa-cloud-download");
        return tooltipDiv;
    },
    open : 
        function() {
            toolTipOpen(tooltipDiv);
        },
    close : function (){
        toolTipClose(tooltipDiv)
    }
    });



    topDiv.append(gameHeader);
    var clockhtml = game_info.clock;
    if (game_info.additionalSeconds !== 0) {
        clockhtml = clockhtml + "+" + game_info.additionalSeconds;
    }
    var clock = $("<i>").addClass("fa").addClass("fa-clock-o").addClass("game_list_clocks").html(clockhtml);
    var white = $("<a>").attr('href', '#').addClass("game_white_player");
    var black = $("<a>").attr('href', '#').addClass("game_black_player");
    var span = $("<span>");
    gu_updateBlackAndWhiteLabels(white, black, game_info);
    span.append(white);
    span.append(clock);
    span.append(black);
    topDiv.append(span);
    elem.append(topDiv);

    $("#active_game").append(elem);
}

function gu_updateBlackAndWhiteLabels(white, black, game_info) {
    if (game_info.white != undefined)
        white.html(game_info.white);
    if (game_info.black != undefined)
        black.html(game_info.black);
}

function gu_updateGameInfo(gameId, game_info) {
    var gameContainer = $("#game-" + gameId);
    gu_updateBlackAndWhiteLabels(gameContainer.find('.game_white_player'),
        gameContainer.find('.game_black_player'), game_info);
}

function gu_removeGame(gameId) {
    $('#game-' + gameId).remove();
}

function gu_setCurrentGame(gameId) {
    gu_removeCurrentGameHighlight();
    $('#game-' + gameId).addClass('current_game');
}

function gu_removeCurrentGameHighlight() {
    $('#active_game').find('.current_game').removeClass('current_game');
}

function gu_gameInfoFromGameContainer(game_container) {

    return {
        'black': game_container.blackPlayer,
        'white': game_container.whitePlayer,
        'clock': game_container.clock,
        'additionalSeconds': game_container.additionalSeconds
    }
}


//dialog stuff
//callback = function(selectedColor,perSide,additinalSecs,stringFen,vsAI)
function gu_createNewGameDialogInvoke(callback) {
    var chess = new Chess();
    var dialogElem = $("<div>").attr("id", "newGameDialog")
        .attr("title", "New game")
        .addClass("newgamedialog");

    var sliderPerSide = $("<div>").attr("dim", "min")
        .attr("id", "sliderTimePerSide")
        .addClass("full_width");
    var sliderAdditionalSec = $("<div>").attr("dim", "sec")
        .attr("id", "sliderTimePerSide")
        .addClass("full_width");

    var fenField = $("<input>").attr("id", "fenfield")
        .attr("type", "text")
        .attr("value", chess.START_FEN)
        .attr("disabled", "true").addClass("ui-state-disabled width95");

    var editFen = $("<a>").attr("href", "#")
        .attr("id", "fenEdit")
        .addClass("fa fa-edit")
        .click(function () {
            var curVal = fenField[0].disabled;
            fenField[0].disabled = !curVal;
        });

    /*Versus AI Toggle Link*/
    var vsAIselected = false;
    var vsAIToggle = $("<a>",{'href': "#" , 'class' :"versus_ai_toggle"})
        .append($("<i>",{'class' : "fa fa-desktop"}))
        .append($("<span>").text(" Play versus engine "));
    var UNSELECTED_COLOR = "#d67874";
    var SELECTED_COLOR = "rgb(161, 219, 22)";
    vsAIToggle.css("background", UNSELECTED_COLOR).click(function () {
        vsAIselected = !vsAIselected;
        $(this).css("background", vsAIselected ? SELECTED_COLOR : UNSELECTED_COLOR);
    });

    /* SLIDERS */
    var perSideDefVal = 10;
    var addSecDefVal = 2;
    sliderPerSide.attr("val", perSideDefVal);
    sliderAdditionalSec.attr("val", addSecDefVal);

    //Update value under slider
    var updateLabel = function (evt, index) {
        $(this).parent().find(".slider_val").html(index.value + " " + $(this).attr("dim"));
        $(this).attr("val", index.value);
    };

    sliderPerSide.slider({
        range: false,
        values: [perSideDefVal],
        min: 1, max: 20, step: 1,
        slide: updateLabel
    });

    sliderAdditionalSec.slider({
        range: false,
        values: [addSecDefVal],
        min: 0, max: 10, step: 1,
        slide: updateLabel
    });

    /*COLOR SELECT RADIOSET BUTTONS*/
    var SelectedColor = "RANDOM"; //Default = random
    var make_input = function (id, value, labelclass, text, checked) {
        var input = $("<input>").attr("type", "radio").attr("id", id).attr("value", value).attr("name", "radio");
        if (checked)
            input.attr("checked", true);
        var label = $("<label>").addClass("colorselect").addClass(labelclass).attr("for", id).html(text);
        return [input, label]
    };
    var radioSet = $("<div>").attr("id", "colorRadioSet").addClass("full_width");

    radioSet.append(make_input("blackradio", "BLACK", "blackColor", "Black", false));
    radioSet.append(make_input("randomradio", "RANDOM", "randomColor", "Random", true));
    radioSet.append(make_input("whiteradio", "WHITE", "whiteColor", "White", false));

    radioSet.buttonset();

    radioSet.find("input[type=radio]").change(function (event) {
        SelectedColor = $(this).val();
        console.log("You selected : " + SelectedColor);
    }).focus(function(){
        $(this).blur(); //Disable focusing
    });

    /* Setup position */
    var cgDiv = $("<div>").addClass("blue_modern yazart boardPreview").attr("id", "ground");
    var chessGround = Chessground(cgDiv[0], {
            orientation: 'white',
            coordinates: false,
            viewOnly: true
        }
    );


    //If user fen is not valid highlight fen text field
    var tickFenRed = function () {
        fenField.addClass("ui-state-error");
        setTimeout(function () {
            fenField.removeClass("ui-state-error")
        }, 2000);
    };

    fenField.on('input', function (e) {
        if (!chess.validate_fen(fenField.val()).valid) {
            tickFenRed();
        }
        else {
            chessGround.set({fen: fenField.val()});
        }
    });


    /* Append stuff to dialog */
    dialogElem.append($("<form>").append(radioSet).css("text-align", "center"));

    dialogElem.append($("<div>")
        .append($("<span>").addClass("full_width center_align param_header").html("Total time"))
        .append(sliderPerSide)
        .append($("<span>").html(perSideDefVal + " min").addClass("full_width").addClass("slider_val center_align"))
        .addClass("full_width"));
    dialogElem.append($("<div>")
        .append($("<span>").addClass("full_width center_align param_header").html("Additional time per move"))
        .append(sliderAdditionalSec)
        .append($("<span>").addClass("slider_val center_align").addClass("full_width").html(addSecDefVal + " sec"))
        .addClass("full_width"));

    dialogElem.append($("<div>")
        .append($("<span>").addClass("full_width center_align param_header").html("Setup position"))
        .append($("<span>").addClass("full_width center_align").append(cgDiv))
        .append($("<span>").append(fenField))
        .append($("<span>").append(editFen).addClass("editFenSpan")));

    dialogElem.append($("<div>",{'class' : 'full_width center_align'}).append(vsAIToggle));

    dialogElem.dialog({
        autoOpen: false,
        width: 500,
        modal: true,
        resizable: false,
        draggable: true,
        close: function () {
            $(this).dialog("destroy");
        },
        show: "slide",
        buttons: [
            {
                text: "Ok",
                click: function () {
                    if (!chess.validate_fen(fenField.val()).valid) {
                        tickFenRed();
                    }
                    else {
                        $(this).dialog("close");
                        callback(SelectedColor, sliderPerSide.attr("val"),
                            sliderAdditionalSec.attr("val"),
                            fenField.val(),
                            vsAIselected);
                        
                    }
                }
            }
        ]
    });

    dialogElem.dialog("open");
    return dialogElem;
}

function gu_invokedbg() {
    gu_createNewGameDialogInvoke(function (perSide, addSec, fen, vsAI) {
        console.log("gu invokedbg: " + perSide + " " + addSec + " " + fen + " " + vsAI);
    });
}


//Callback :: function(piece), piece : q | r | b | k; isWhite :: boolean()
function gu_invokeSelectPromotionDialog(callBack, isWhite) {
    var chess = new Chess();

    function makeObj(Val, Clazz, Text, Checked) {
        return {val: Val, clazz: isWhite ? Clazz : "b" + Clazz, text: Text, checked: Checked};
    }

    var piece;

    var Input = [makeObj(chess.QUEEN, "psqueen", "Queen", false),
        makeObj(chess.ROOK, "psrook", "Rook", false),
        makeObj(chess.BISHOP, "psbishop", "Bishop", false),
        makeObj(chess.KNIGHT, "psknight", "Knight", false)];

    var radioSet = $("<div>").attr("id", "pieceset");

    var input, label;
    $.each(Input,function(index,e) {
        input = $("<input>").attr("type", "radio").attr("id", "radio" + index).attr("name", "radio").attr("value", e.val);
        if (e.checked) {
            input.attr("checked", true);
            piece = e.val;
        }
        label = $("<label>").attr("for", "radio" + index).addClass("promotionSelect").addClass(e.clazz).html(e.text);
        radioSet.append(input);
        radioSet.append(label);
    });


    radioSet.buttonset();

    var dialogElem = $("<div>").attr("id", "selectPieceDialog")
        .attr("title", "Choose Promotion");
    dialogElem.append($("<form>").append(radioSet));

    dialogElem.dialog({
        autoOpen: false,
        width: 450,
        modal: true,
        resizable: false,
        draggable: false,
        close: function () {
            callBack(piece);
            $(this).dialog("destroy");
        }
    });

    radioSet.find("input[type=radio]").change(function (event) {
        piece = $(this).val();
        dialogElem.dialog("close");
    }).focus(function(){
        $(this).blur();
    });

    dialogElem.dialog("open");
    return dialogElem;
}
