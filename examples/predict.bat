java -jar -Duser.language=ru -Duser.region=RU libs/train-@project.version@.jar ^
--model input/model.xlsx ^
--predictSetFile input/predictionSetReal.xlsx ^
--predictOutputFile output/predict_output.xlsx
pause