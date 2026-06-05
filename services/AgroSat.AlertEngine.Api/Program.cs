using AgroShield.AlertEngine.Api.Services;
using AgroShield.AlertEngine.Api.Data;
using Microsoft.EntityFrameworkCore;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new()
    {
        Title = "AgroShield AlertEngine API",
        Version = "v1",
        Description = "Servico de composicao de alertas agricolas (RF-IA parcial). " +
                      "Consumido pelo backend Java; TTS em Python."
    });
});

builder.Services.AddScoped<IAlertCompositionService, AlertCompositionService>();

// Configurar DbContext com MySQL
builder.Services.AddDbContext<AgroShieldDbContext>(options =>
    options.UseMySql(
        builder.Configuration.GetConnectionString("DefaultConnection"),
        new MySqlServerVersion(new Version(8, 0, 33))
    ));

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.MapControllers();

app.Run();
