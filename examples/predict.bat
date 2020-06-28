java -jar -Duser.language=ru -Duser.region=RU libs/train-@project.version@.jar ^
--model input/trained_model.xlsx ^
--predictSetFile input/predictionSetReal.xlsx ^
--predictOutputFile output/predict_output.xlsx
pause