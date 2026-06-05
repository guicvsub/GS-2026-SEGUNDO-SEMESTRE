using Microsoft.EntityFrameworkCore;
using AgroShield.AlertEngine.Api.Entities;

namespace AgroShield.AlertEngine.Api.Data;

public class AgroShieldDbContext : DbContext
{
    public AgroShieldDbContext(DbContextOptions<AgroShieldDbContext> options) : base(options)
    {
    }

    public DbSet<Terreno> Terrenos { get; set; }
    public DbSet<HistoricoAlerta> HistoricoAlertas { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // Configurar relacionamento Terreno -> HistoricoAlerta
        modelBuilder.Entity<Terreno>()
            .HasMany(t => t.HistoricoAlertas)
            .WithOne(h => h.Terreno)
            .HasForeignKey(h => h.TerrenoId)
            .OnDelete(DeleteBehavior.Cascade);

        // Configurar índices para melhor performance
        modelBuilder.Entity<Terreno>()
            .HasIndex(t => t.Nome);

        modelBuilder.Entity<HistoricoAlerta>()
            .HasIndex(h => h.TerrenoId);

        modelBuilder.Entity<HistoricoAlerta>()
            .HasIndex(h => h.Codigo);

        modelBuilder.Entity<HistoricoAlerta>()
            .HasIndex(h => h.CriadoEm);
    }
}
